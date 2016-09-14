package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cheetahplatform.web.dto.ChangeSubjectRequest;
import org.cheetahplatform.web.dto.CreateSubjectRequest;
import org.cheetahplatform.web.dto.CreateSubjectResponse;
import org.cheetahplatform.web.dto.PlainSubjectDto;
import org.cheetahplatform.web.dto.StudyDto;
import org.cheetahplatform.web.dto.SubjectDto;
import org.cheetahplatform.web.dto.SubjectForSearchDto;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

public class SubjectDao extends AbstractCheetahDao {

	public void changeSubject(Connection connection, ChangeSubjectRequest changeSubjecRequest) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("UPDATE subject set email=?, subject_id=?, comment=? where pk_subject=?");
		statement.setString(1, changeSubjecRequest.getEmail());
		statement.setString(2, changeSubjecRequest.getSubjectId());
		statement.setString(3, changeSubjecRequest.getComment());
		statement.setLong(4, changeSubjecRequest.getId());

		statement.execute();

		statement.close();
	}

	public CreateSubjectResponse createSubject(Connection connection, CreateSubjectRequest createSubjecRequest) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"insert into subject (email, subject_id, fk_study, comment) values (?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
		statement.setString(1, createSubjecRequest.getEmail());
		statement.setString(2, createSubjecRequest.getSubjectId());
		statement.setLong(3, createSubjecRequest.getStudyId());
		statement.setString(4, createSubjecRequest.getComment());

		statement.execute();

		ResultSet keys = statement.getGeneratedKeys();
		keys.next();
		long id = keys.getLong(1);
		statement.close();

		return new CreateSubjectResponse(id, createSubjecRequest.getEmail(), createSubjecRequest.getSubjectId(),
				createSubjecRequest.getStudyId(), createSubjecRequest.getComment());

	}

	public void deleteSubject(Connection connection, long id) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("delete from subject where pk_subject = ?");
		statement.setLong(1, id);
		statement.execute();
		statement.close();
	}

	private List<PlainSubjectDto> extractPlainSubjects(ResultSet result) throws SQLException {
		List<PlainSubjectDto> subjects = new ArrayList<>();
		while (result.next()) {
			long id = result.getLong("pk_subject");
			String email = result.getString("email");
			String subjectId = result.getString("subject_id");
			long study = result.getLong("fk_study");
			String comment = result.getString("comment");
			long synchronizedFrom = result.getLong("synchronized_from");

			subjects.add(new PlainSubjectDto(id, email, subjectId, study, comment, synchronizedFrom));
		}

		return subjects;
	}

	private List<SubjectDto> extractSubjects(ResultSet result) throws SQLException {
		List<SubjectDto> subjects = new ArrayList<>();
		while (result.next()) {
			long subjectId = result.getLong(1);
			String name = result.getString(2);
			String study = result.getString(3);
			subjects.add(new SubjectDto(subjectId, name, study));
		}

		return subjects;
	}

	public List<SubjectForSearchDto> getAllSubjectsForUser(Connection connection, long userId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"select pk_subject, email, subject_id, subject.comment, study.pk_study, study.name from subject join studies_to_user on studies_to_user.fk_study = subject.fk_study join study on subject.fk_study = study.pk_study where studies_to_user.fk_user = ?;");
		statement.setLong(1, userId);

		List<SubjectForSearchDto> subjects = new ArrayList<>();
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			long id = resultSet.getLong("pk_subject");
			String email = resultSet.getString("email");
			String subjectId = resultSet.getString("subject_id");
			String comment = resultSet.getString("comment");

			long studyId = resultSet.getLong("pk_study");
			String studyName = resultSet.getString("name");
			StudyDto study = new StudyDto(studyId, studyName, null);
			SubjectForSearchDto subject = new SubjectForSearchDto(id, email, subjectId, study, comment);
			subjects.add(subject);
		}

		statement.close();
		return subjects;
	}

	public Map<Long, Long> getStudyIdsForSubjects(Set<Long> subjectIds) throws SQLException {
		if (subjectIds.isEmpty()) {
			return Collections.emptyMap();
		}

		Connection connection = AbstractCheetahServlet.getDatabaseConnection();

		String in = buildIn(subjectIds);
		String sql = "select pk_subject, fk_study from subject where pk_subject in (" + in + ");";
		PreparedStatement statement = connection.prepareStatement(sql);

		Map<Long, Long> mapping = new HashMap<>();
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long subjectId = result.getLong(1);
			long studyId = result.getLong(2);
			mapping.put(subjectId, studyId);
		}
		cleanUp(connection, statement, result);

		return mapping;
	}

	public SubjectDto getSubjectForPpmInstanceId(long userId, long ppmInstanceId) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = connection.prepareStatement(
				"select sub.pk_subject, sub.subject_id, st.name, p.id, p.notation from subject sub, study st, process p, studies_to_user sttu, process_instance pi where sub.fk_study=st.pk_study and sttu.fk_study=st.pk_study AND sttu.fk_user=? AND pi.fk_subject=sub.pk_subject and pi.database_id=?;");
		statement.setLong(1, userId);
		statement.setLong(2, ppmInstanceId);
		ResultSet result = statement.executeQuery();

		List<SubjectDto> subjects = extractSubjects(result);
		result.close();
		connection.close();
		return subjects.get(0);
	}

	public List<PlainSubjectDto> getSubjectsFor(Connection connection, long studyId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select * from subject where fk_study = ?");
		statement.setLong(1, studyId);
		ResultSet sourceSubjectsResult = statement.executeQuery();
		List<PlainSubjectDto> subjects = extractPlainSubjects(sourceSubjectsResult);
		sourceSubjectsResult.close();
		statement.close();
		return subjects;
	}

	public List<PlainSubjectDto> getSubjectsForSynchronizedStudyId(Connection connection, long synchronizedStudyId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"select subject.pk_subject, subject.email, subject.subject_id, subject.fk_study, subject.comment, subject.synchronized_from from subject, study where study.pk_study = subject.fk_study and study.synchronized_from = ?");
		statement.setLong(1, synchronizedStudyId);
		ResultSet sourceSubjectsResult = statement.executeQuery();
		List<PlainSubjectDto> subjects = extractPlainSubjects(sourceSubjectsResult);
		sourceSubjectsResult.close();
		statement.close();

		return subjects;
	}

	public SubjectDto getSubjectWithId(long userId, long subjectId) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = connection.prepareStatement(
				"SELECT sub.pk_subject, sub.subject_id, st.name FROM subject sub, study st, studies_to_user sttu WHERE sub.fk_study = st.pk_study AND sttu.fk_study = st.pk_study AND sttu.fk_user = ? AND sub.pk_subject = ?;");
		statement.setLong(1, userId);
		statement.setLong(2, subjectId);
		ResultSet result = statement.executeQuery();
		List<SubjectDto> subjects = extractSubjects(result);
		cleanUp(connection, statement, result);

		if (subjects == null || subjects.isEmpty()) {
			return null;
		}

		return subjects.get(0);
	}

	public SubjectDto getSubjectWithName(Connection connection, long userId, String subjectName) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"SELECT sub.pk_subject, sub.subject_id, st.name FROM subject sub, study st, studies_to_user sttu WHERE sub.fk_study = st.pk_study AND sttu.fk_study = st.pk_study AND sttu.fk_user = ? AND sub.subject_id = ?;");
		statement.setLong(1, userId);
		statement.setString(2, subjectName);
		ResultSet result = statement.executeQuery();
		List<SubjectDto> subjects = extractSubjects(result);
		result.close();

		if (subjects.size() > 1) {
			new NotificationDao().insertNotification(connection, "Found more than one subject with subject id " + subjectName,
					NotificationDao.NOTIFICATION_ERROR, userId);
			return null;
		} else if (subjects.isEmpty()) {
			return null;
		}

		return subjects.get(0);
	}

	public PlainSubjectDto insertSynchronizedSubject(Connection connection, PlainSubjectDto synchronizedFrom, long studyId)
			throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				"insert into subject (email,subject_id,fk_study,comment,synchronized_from) values (?,?,?,?,?);",
				PreparedStatement.RETURN_GENERATED_KEYS);
		statement.setString(1, synchronizedFrom.getEmail());
		statement.setString(2, synchronizedFrom.getSubjectId());
		statement.setLong(3, studyId);
		statement.setString(4, synchronizedFrom.getComment());
		statement.setLong(5, synchronizedFrom.getId());
		statement.execute();

		ResultSet keys = statement.getGeneratedKeys();
		keys.next();
		long id = keys.getLong(1);
		statement.close();

		return new PlainSubjectDto(id, synchronizedFrom.getEmail(), synchronizedFrom.getSubjectId(), studyId, synchronizedFrom.getComment(),
				synchronizedFrom.getId());
	}

	public boolean subjectExists(Connection connection, String email) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select count(*) from subject where email = ?");
		statement.setString(1, email);
		ResultSet resultSet = statement.executeQuery();
		resultSet.next();
		int existingStudyCount = resultSet.getInt(1);
		return existingStudyCount > 0;
	}

	public boolean subjectIDExistsInStudy(Connection connection, String subjectID, long study) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select count(*) from subject where subject_id = ? AND fk_study = ?");
		statement.setString(1, subjectID);
		statement.setLong(2, study);
		ResultSet resultSet = statement.executeQuery();
		resultSet.next();
		int existingStudyCount = resultSet.getInt(1);
		return existingStudyCount > 0;
	}
}
