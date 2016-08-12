package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.dto.PlainSubjectDto;
import org.cheetahplatform.web.dto.StudyDto;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;

public class StudyDao {
	/**
	 * Queries and adds the data processing for a given list of studies.
	 *
	 * @param connection
	 * @param studies
	 * @throws SQLException
	 */
	public void addDataProcessing(Connection connection, List<StudyDto> studies) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select * from data_processing where fk_study = ?");
		for (StudyDto study : studies) {
			statement.setLong(1, study.getId());
			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
				long id = resultSet.getLong("pk_data_processing");
				String name = resultSet.getString("name");
				String comment = resultSet.getString("comment");

				DataProcessing dataProcessing = new DataProcessing(id, name, comment);
				study.addDataProcessing(dataProcessing);
			}
		}
	}

	private void assignUserToStudy(Connection connection, long userId, long id) throws SQLException {
		PreparedStatement assignmentStatement = connection.prepareStatement("insert into studies_to_user (fk_user,fk_study) values (?, ?)");
		assignmentStatement.setLong(1, userId);
		assignmentStatement.setLong(2, id);
		assignmentStatement.execute();
		assignmentStatement.close();
	}

	public String canStudyBeDeleted(Connection connection, long studyId, long userId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select count(*) from studies_to_user where fk_study=? and fk_user=?");
		statement.setLong(1, studyId);
		statement.setLong(2, userId);
		ResultSet result = statement.executeQuery();
		result.next();
		int count = result.getInt(1);
		result.close();
		statement.close();
		if (count != 1) {
			return "The user does not own the study";
		}

		SubjectDao subjectDao = new SubjectDao();
		List<PlainSubjectDto> subjects = subjectDao.getSubjectsFor(connection, studyId);
		boolean empty = subjects.isEmpty();
		if (!empty) {
			return "There are already subjects assigned to the study.";
		}
		return null;
	}

	public void deleteStudy(Connection connection, long studyId, long userId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("delete from studies_to_user where fk_user=? and fk_study=?");
		statement.setLong(1, userId);
		statement.setLong(2, studyId);
		statement.executeUpdate();
		statement.close();

	}

	public List<StudyDto> getStudies(Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		ResultSet studyResult = statement.executeQuery("select * from study");
		List<StudyDto> studies = new ArrayList<>();

		while (studyResult.next()) {
			long id = studyResult.getLong("pk_study");
			String name = studyResult.getString("name");
			String comment = studyResult.getString("comment");

			studies.add(new StudyDto(id, name, comment));
		}
		studyResult.close();
		statement.close();
		return studies;
	}

	public List<StudyDto> getStudiesForUser(Connection connection, long userId) throws SQLException {
		PreparedStatement statement = connection
				.prepareStatement("SELECT * FROM studies_to_user JOIN study on(pk_study=fk_study) where fk_user = ? ");
		statement.setLong(1, userId);
		ResultSet studyResult = statement.executeQuery();
		List<StudyDto> studies = new ArrayList<>();

		while (studyResult.next()) {
			long id = studyResult.getLong("pk_study");
			String name = studyResult.getString("name");
			String comment = studyResult.getString("comment");

			studies.add(new StudyDto(id, name, comment));
		}
		studyResult.close();
		statement.close();
		return studies;

	}

	public void insertStudy(Connection connection, long userId, String name, String comment) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("insert into study (name,comment) values (?,?);",
				PreparedStatement.RETURN_GENERATED_KEYS);
		statement.setString(1, name);
		statement.setString(2, comment);
		statement.execute();

		ResultSet keys = statement.getGeneratedKeys();
		keys.next();
		long id = keys.getLong(1);
		keys.close();
		statement.close();

		assignUserToStudy(connection, userId, id);
	}

	/**
	 * Inserts a new synchronized study based upon a given study.
	 *
	 * @param connection
	 * @param study
	 * @param userId
	 * @return
	 * @throws SQLException
	 */
	public StudyDto insertSynchronizedStudy(Connection connection, StudyDto study, long userId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("insert into study (name,comment,synchronized_from) values (?,?,?);",
				PreparedStatement.RETURN_GENERATED_KEYS);
		statement.setString(1, study.getName());
		statement.setString(2, study.getComment());
		if (study.getId() == null) {
			statement.setNull(3, Types.BIGINT);
		} else {
			statement.setLong(3, study.getId());
		}
		statement.execute();

		ResultSet keys = statement.getGeneratedKeys();
		keys.next();
		long id = keys.getLong(1);
		keys.close();

		StudyDto synchronizedStudy = new StudyDto(id, study.getName(), study.getComment());
		synchronizedStudy.setSynchronizedFrom(study.getId());
		statement.close();

		// assign study to user
		assignUserToStudy(connection, userId, id);

		return synchronizedStudy;
	}

}
