package org.cheetahplatform.web.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.dto.StudyDto;

public class StudyDao {
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
		PreparedStatement assignmentStatement = connection.prepareStatement("insert into studies_to_user (fk_user,fk_study) values (?, ?)");
		assignmentStatement.setLong(1, userId);
		assignmentStatement.setLong(2, id);
		assignmentStatement.execute();
		assignmentStatement.close();

		return synchronizedStudy;
	}

}
