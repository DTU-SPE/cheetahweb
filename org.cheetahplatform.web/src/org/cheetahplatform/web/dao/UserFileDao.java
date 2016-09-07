package org.cheetahplatform.web.dao;

import static org.cheetahplatform.common.Assert.isTrue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.cheetahplatform.web.dto.SubjectDto;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.dto.UserFileTagDto;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

import com.mysql.jdbc.Statement;

public class UserFileDao extends AbstractCheetahDao {
	public static final String WEBAPPS_PATH = "/webapps";
	public static final String USER_UPLOAD_PATH = WEBAPPS_PATH + "/cheetah_content/user_upload";
	public static final String VIDEO_PATH = WEBAPPS_PATH + "/cheetah_content/videos";
	public static final String ARCHIVE_PATH = WEBAPPS_PATH + "/cheetah_content/archive";

	public static final String TAG_RAW_DATA = "raw-data";
	public static final String TAG_TRIMMED = "trimmed";
	public static final String TAG_CLEANED = "cleaned";
	public static final String TAG_VIDEO = "video";
	public static final String TAG_RESULT = "result";
	public static final List<String> STANDARD_TAGS;

	static {
		STANDARD_TAGS = new ArrayList<>();
		STANDARD_TAGS.add(TAG_RAW_DATA);
		STANDARD_TAGS.add(TAG_TRIMMED);
		STANDARD_TAGS.add(TAG_CLEANED);
		STANDARD_TAGS.add(TAG_VIDEO);
		STANDARD_TAGS.add(TAG_RESULT);
	}

	public static File getPath(String type) {
		File catalinaBase = new File(System.getProperty("catalina.base")).getAbsoluteFile();
		File archive = new File(catalinaBase, type);
		if (!archive.exists()) {
			archive.mkdirs();
		}

		return archive;
	}

	private void addStudyToFiles(List<UserFileDto> files) throws SQLException {
		Set<Long> ids = new java.util.HashSet<>();
		for (UserFileDto userFileDto : files) {
			Long subjectId = userFileDto.getSubjectId();
			if (subjectId == null) {
				continue;
			}

			ids.add(subjectId);
		}

		Map<Long, Long> studyIdsForSubjects = new SubjectDao().getStudyIdsForSubjects(ids);
		for (UserFileDto userFileDto : files) {
			Long subjectId = userFileDto.getSubjectId();
			Long studyId = studyIdsForSubjects.get(subjectId);
			userFileDto.setStudyId(studyId);
		}
	}

	public void addTags(Long fileId, String... tags) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		insertTags(Arrays.asList(fileId), Arrays.asList(tags), connection);
		connection.close();
	}

	private void addTagsToFile(UserFileDto file) throws SQLException {
		List<UserFileDto> files = new ArrayList<>();
		files.add(file);
		addTagsToFiles(files);
	}

	private void addTagsToFiles(List<UserFileDto> files) throws SQLException {
		if (files.isEmpty()) {
			return;
		}

		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		Map<Long, UserFileDto> ids = new HashMap<>();
		for (UserFileDto userFileDto : files) {
			ids.put(userFileDto.getId(), userFileDto);
		}

		PreparedStatement statement = connection.prepareStatement(
				"select pk_user_data_tags, tag, fk_user_data from user_data_tags where fk_user_data in(" + buildIn(ids.keySet()) + ");");
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long tagId = result.getLong(1);
			String tag = result.getString(2);
			long fileId = result.getLong(3);
			ids.get(fileId).addTag(new UserFileTagDto(tagId, tag));
		}
		cleanUp(connection, statement, result);
	}

	public void deleteFilePermanently(Connection connection, long fileId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("delete from user_data where pk_user_data = ?");
		statement.setLong(1, fileId);
		statement.execute();
	}

	public void deleteFiles(List<Long> filesToDelete, long userid) throws SQLException {
		String in = buildIn(filesToDelete);
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = connection
				.prepareStatement("update user_data set hidden=true where fk_user=? and pk_user_data in (" + in + ")");
		statement.setLong(1, userid);

		statement.executeUpdate();
		cleanUp(connection, statement);
	}

	public void deleteTags(long fileId, String... tags) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		StringBuilder questionMarks = new StringBuilder();
		for (int i = 0; i < tags.length; i++) {
			questionMarks.append("?");
			if (i != tags.length - 1) {
				questionMarks.append(", ");
			}
		}
		String query = "delete from user_data_tags where fk_user_data = ? and tag in (" + questionMarks.toString() + ")";
		PreparedStatement deleteStatement = connection.prepareStatement(query);
		deleteStatement.setLong(1, fileId);
		for (int i = 0; i < tags.length; i++) {
			deleteStatement.setString(i + 2, tags[i]);
		}
		deleteStatement.executeUpdate();
		cleanUp(connection, deleteStatement);
	}

	private UserFileDto extractFile(ResultSet result, boolean extractProcessInstance) throws SQLException {
		long id = result.getLong("pk_user_data");
		String filename = result.getString("filename");
		String type = null;
		if (extractProcessInstance) {
			type = result.getString("type");
		} else {
			type = result.getString("user_data.type");
		}
		String path = result.getString("path");
		String url = path.replaceAll("webapps", "../..");
		String comment = result.getString("comment");
		Long subjectId = result.getLong("user_data.fk_subject");
		if (result.wasNull()) {
			subjectId = null;
		}

		UserFileDto userFileDto = new UserFileDto(id, filename, type, url, comment, subjectId);
		if (extractProcessInstance) {
			long processInstanceId = result.getLong("fk_process_instance");
			if (!result.wasNull()) {
				userFileDto.setProcessInstanceId(processInstanceId);
			}

			String processInstanceName = result.getString("process.id");
			userFileDto.setProcessInstanceName(processInstanceName);
		}

		return userFileDto;
	}

	public List<UserFileDto> extractFiles(ResultSet result, boolean extractProcessInstance) throws SQLException {
		List<UserFileDto> files = new ArrayList<UserFileDto>();
		while (result.next()) {
			files.add(extractFile(result, extractProcessInstance));
		}
		addTagsToFiles(files);
		addStudyToFiles(files);
		return files;
	}

	/**
	 * Retrieves all user files for a subject that might be still connected.
	 *
	 * @param connection
	 * @param subjectId
	 * @return
	 * @throws SQLException
	 */
	public List<UserFileDto> findConnectCandidates(Connection connection, long subjectId) throws SQLException {
		String query = "select * from user_data where fk_derived_from is null and fk_subject = ? and hidden = ? and not exists (select fk_derived_from from user_data user_data_sub where user_data_sub.fk_derived_from = user_data.pk_user_data union select fk_user_file from eyetracking_movie where fk_user_file = user_data.pk_user_data)";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setLong(1, subjectId);
		statement.setBoolean(2, false);
		ResultSet resultSet = statement.executeQuery();
		return extractFiles(resultSet, false);
	}

	public String generateRelativeMoviePath(long userId, String movieFileName) {
		return internalGeneratePath(VIDEO_PATH, userId, movieFileName);
	}

	public String generateRelativePath(long userId, String fileName) {
		return internalGeneratePath(USER_UPLOAD_PATH, userId, fileName);
	}

	public String getAbsolutePath(String relativePath) {
		File catalinaBase = new File(System.getProperty("catalina.base")).getAbsoluteFile();
		return catalinaBase.getAbsolutePath() + relativePath;
	}

	public List<UserFileDto> getDerivedFiles(Connection connection, Long derivedFromId) throws SQLException {
		String sql = "select * from user_data where fk_derived_from = ?;";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setLong(1, derivedFromId);
		ResultSet resultSet = statement.executeQuery();
		List<UserFileDto> files = new ArrayList<>();
		while (resultSet.next()) {
			UserFileDto file = extractFile(resultSet, false);
			files.add(file);
		}

		statement.close();
		return files;
	}

	public List<UserFileDto> getEyeTrackingDataForPpmInstance(long ppmInstanceId) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = connection.prepareStatement("select * from user_data where fk_process_instance=?");
		statement.setLong(1, ppmInstanceId);

		List<UserFileDto> files = new ArrayList<>();
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			UserFileDto file = extractFile(result, false);
			files.add(file);
		}

		cleanUp(connection, statement, result);
		addTagsToFiles(files);
		addStudyToFiles(files);
		return files;
	}

	public UserFileDto getFile(long fileId) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = connection.prepareStatement("select * from user_data where pk_user_data=?");
		statement.setLong(1, fileId);
		ResultSet result = statement.executeQuery();
		UserFileDto file = null;
		while (result.next()) {
			file = extractFile(result, false);
		}
		cleanUp(connection, statement, result);

		addTagsToFile(file);
		addStudyToFiles(Arrays.asList(file));

		return file;
	}

	/**
	 * Retrieves all user files with a given name.
	 *
	 * @param expectedName
	 * @throws SQLException
	 */
	public List<UserFileDto> getFileByName(Connection connection, String expectedName) throws SQLException {
		return internalGetFile(connection, "select * from user_data where filename = ?;", expectedName);
	}

	public List<UserFileDto> getFileWithNameLike(Connection connection, String pattern) throws SQLException {
		return internalGetFile(connection, "select * from user_data where filename like ?;", pattern);
	}

	public String getPath(long fileId) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = connection.prepareStatement("select path from user_data where pk_user_data=?");

		statement.setLong(1, fileId);

		ResultSet result = statement.executeQuery();
		String path = null;
		while (result.next()) {
			path = result.getString(1);
		}

		cleanUp(connection, statement, result);
		return path;
	}

	public Map<Long, String> getPaths(List<Long> files) throws SQLException {
		Map<Long, String> map = new HashMap<Long, String>();

		String inString = buildIn(files);

		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		String sql = "select pk_user_data, path from user_data where pk_user_data in (" + inString + ")";
		PreparedStatement statement = connection.prepareStatement(sql);

		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long id = result.getLong(1);
			String path = result.getString(2);
			map.put(id, path);
		}

		cleanUp(connection, statement, result);
		return map;
	}

	public Map<Long, Long> getPpmInstancesForFiles(long userId, List<Long> fileIds) throws SQLException {
		String in = buildIn(fileIds);

		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = connection.prepareStatement(
				"select pk_user_data, fk_process_instance from user_data where fk_user=? AND pk_user_data in ( " + in + " )");
		statement.setLong(1, userId);

		Map<Long, Long> result = new HashMap<>();
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			long fileId = resultSet.getLong(1);
			long ppmInstanceId = resultSet.getLong(2);
			result.put(fileId, ppmInstanceId);
		}
		cleanUp(connection, statement, resultSet);
		return result;
	}

	public Integer getUsageOfForeignKeyOfSubject(Connection connection, long subjectId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"Select count(un.fk_subject) as resultat from (SELECT pi.fk_subject from process_instance as pi union all SELECT su.fk_subject from user_data as su) as un where un.fk_subject=?;");
		statement.setLong(1, subjectId);
		ResultSet resultSet = statement.executeQuery();
		resultSet.next();
		Integer usageOfFK = resultSet.getInt("resultat");
		statement.close();

		return usageOfFK;
	}

	public File getUserFile(String path) {
		File catalinaBase = new File(System.getProperty("catalina.base")).getAbsoluteFile();
		return new File(catalinaBase.getAbsolutePath() + path);
	}

	public List<UserFileDto> getUserFiles(long userid) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = connection.prepareStatement("select * from user_data where fk_user=? and hidden=?");
		statement.setLong(1, userid);
		statement.setBoolean(2, false);
		ResultSet result = statement.executeQuery();

		List<UserFileDto> files = extractFiles(result, false);
		cleanUp(connection, statement, result);
		return files;
	}

	public List<UserFileDto> getUserFilesForSubject(Connection connection, long subjectId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"select process.id, pk_user_data, filename, user_data.type, path, comment, fk_process_instance, user_data.fk_subject from user_data left outer join process_instance on fk_process_instance = process_instance.database_id left outer join process on process_instance.process = process.database_id where user_data.fk_subject =?;");
		statement.setLong(1, subjectId);
		ResultSet resultSet = statement.executeQuery();
		List<UserFileDto> files = extractFiles(resultSet, true);
		statement.close();

		return files;
	}

	private void insertTags(List<Long> fileIds, Collection<String> tags, Connection connection) throws SQLException {
		PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO user_data_tags (fk_user_data, tag) VALUES (?, ?);");
		for (String tag : tags) {
			for (Long fileId : fileIds) {
				insertStatement.setLong(1, fileId);
				insertStatement.setString(2, tag);
				insertStatement.executeUpdate();
			}
		}
		insertStatement.close();
	}

	public long insertUserFile(long userId, String name, String relativeFilePath, String type) throws SQLException {
		return insertUserFile(userId, name, relativeFilePath, type, null);
	}

	public long insertUserFile(long userId, String name, String relativeFilePath, String type, String comment) throws SQLException {
		return insertUserFile(userId, name, relativeFilePath, type, comment, null, null, false, null);
	}

	public long insertUserFile(long userId, String name, String relativeFilePath, String type, String comment, Long processInstanceId,
			Long subject, boolean hidden, Long derivedFrom) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement insertStatement = connection.prepareStatement(
				"insert into user_data (filename, path, type, fk_user, comment, fk_process_instance, fk_subject, hidden, fk_derived_from) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
		insertStatement.setString(1, name);
		insertStatement.setString(2, relativeFilePath);
		insertStatement.setString(3, type);
		insertStatement.setLong(4, userId);
		insertStatement.setString(5, comment);

		if (processInstanceId == null) {
			insertStatement.setNull(6, Types.BIGINT);
		} else {
			insertStatement.setLong(6, processInstanceId);
		}
		if (subject == null) {
			insertStatement.setNull(7, Types.BIGINT);
		} else {
			insertStatement.setLong(7, subject);
		}
		insertStatement.setBoolean(8, hidden);
		if (derivedFrom == null) {
			insertStatement.setNull(9, Types.BIGINT);
		} else {
			insertStatement.setLong(9, derivedFrom);
		}

		insertStatement.executeUpdate();

		ResultSet keys = insertStatement.getGeneratedKeys();
		keys.next();
		long userFileId = keys.getLong(1);

		cleanUp(connection, insertStatement);

		return userFileId;
	}

	private String internalGeneratePath(String prefix, long userId, String fileName) {
		File catalinaBase = new File(System.getProperty("catalina.base")).getAbsoluteFile();
		String userDirectoryPath = prefix + "/" + userId;
		File userDirectory = new File(catalinaBase.getAbsolutePath() + userDirectoryPath);
		if (!userDirectory.exists()) {
			userDirectory.mkdirs();
		}

		return userDirectoryPath + "/" + System.currentTimeMillis() + "_" + fileName.replaceAll(" ", "_");
	}

	private List<UserFileDto> internalGetFile(Connection connection, String sql, String name) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1, name);
		ResultSet resultSet = statement.executeQuery();
		List<UserFileDto> files = new ArrayList<>();
		while (resultSet.next()) {
			UserFileDto file = extractFile(resultSet, false);
			files.add(file);
		}

		statement.close();
		return files;
	}

	public void mapFileToSubject(Connection connection, Long fileId, Long subjectId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("UPDATE user_data SET fk_subject=?, hidden=? WHERE pk_user_data=?;");
		statement.setLong(1, subjectId);
		statement.setBoolean(2, false);
		statement.setLong(3, fileId);
		statement.executeUpdate();
		statement.close();
	}

	public void removeTags(Connection connection, List<Long> fileIds, List<String> tags) throws SQLException {
		if (fileIds == null || fileIds.isEmpty()) {
			return;
		}
		if (tags == null || tags.isEmpty()) {
			return;
		}

		String fileIdsIn = buildIn(fileIds);
		String tagsIn = buildIn(tags);

		PreparedStatement statement = connection
				.prepareStatement("delete from user_data_tags where fk_user_data in (" + fileIdsIn + ") and tag in (" + tagsIn + ")");
		statement.executeUpdate();
		statement.close();
	}

	public long saveUserFile(long userId, FileItem fileItem, SubjectDto subject, boolean isHidden) throws Exception {
		String name = fileItem.getName();
		String type = fileItem.getContentType();
		InputStream input = fileItem.getInputStream();

		return saveUserFile(userId, name, type, input, subject, isHidden);
	}

	public long saveUserFile(long userId, String name, String type, InputStream input, SubjectDto subject, boolean isHidden)
			throws Exception, IOException, SQLException {
		String relativeFilePath = generateRelativePath(userId, name);
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		try {
			inputStream = new BufferedInputStream(input);
			File file = new File(getAbsolutePath(relativeFilePath));
			if (!file.exists())

				outputStream = new BufferedOutputStream(new FileOutputStream(file));
			IOUtils.copy(inputStream, outputStream);
		} catch (Exception e) {
			throw e;
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}

		Long subjectId = null;
		if (subject != null) {
			subjectId = subject.getSubjectId();
		}
		return insertUserFile(userId, name, relativeFilePath, type, null, null, subjectId, isHidden, null);
	}

	public void updateComment(long fileId, String comment) throws SQLException {
		String sql = "UPDATE user_data SET comment=? WHERE pk_user_data=?;";
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement updateStatement = connection.prepareStatement(sql);
		updateStatement.setString(1, comment);
		updateStatement.setLong(2, fileId);
		updateStatement.executeUpdate();
		cleanUp(connection, updateStatement);
	}

	public void updateFileName(Connection connection, long fileId, String newName) throws SQLException {
		Map<Long, String> map = new HashMap<>();
		map.put(fileId, newName);
		updateFileName(connection, map);
	}

	public void updateFileName(Connection connection, Map<Long, String> newFileNames) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("UPDATE user_data SET filename=? WHERE pk_user_data=?;");

		for (Entry<Long, String> entry : newFileNames.entrySet()) {
			statement.setString(1, entry.getValue());
			statement.setLong(2, entry.getKey());
			statement.executeUpdate();
		}

		statement.close();
	}

	/**
	 * Updates the subject for the given file.
	 *
	 * @param connection
	 * @param file
	 * @param subjectId
	 * @throws SQLException
	 */
	public void updateSubject(Connection connection, long fileId, long subjectId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("update user_data set fk_subject = ? where pk_user_data = ?");
		statement.setLong(1, subjectId);
		statement.setLong(2, fileId);
		int count = statement.executeUpdate();
		isTrue(count == 1);
		statement.close();
	}

	public void updateTags(List<Long> fileIds, Collection<String> tags) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement deleteStatement = connection
				.prepareStatement("delete from user_data_tags where fk_user_data in (" + buildIn(fileIds) + ")");
		deleteStatement.executeUpdate();

		insertTags(fileIds, tags, connection);
		cleanUp(connection, deleteStatement);
	}

	public void updateTags(long fileId, Collection<String> tags) throws SQLException {
		List<Long> ids = new ArrayList<>();
		ids.add(fileId);
		updateTags(ids, tags);
	}
}
