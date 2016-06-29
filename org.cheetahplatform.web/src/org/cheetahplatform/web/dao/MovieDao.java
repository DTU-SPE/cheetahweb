package org.cheetahplatform.web.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.cheetahplatform.web.dto.MovieDto;
import org.cheetahplatform.web.servlet.AbstractCheetahServlet;

public class MovieDao extends AbstractCheetahDao {

	public List<MovieDto> extractMovies(ResultSet result) throws SQLException {
		List<MovieDto> movies = new ArrayList<MovieDto>();
		while (result.next()) {
			String path = result.getString("movie_path");
			String type = result.getString("movie_type");
			long processInstance = result.getLong("process_instance");
			if (result.wasNull()) {
				processInstance = -1;
			}

			long startTimestamp = result.getLong("start_timestamp");
			long movieId = result.getLong("pk_eyetracking_movie");
			movies.add(new MovieDto(path, type, movieId, startTimestamp, processInstance));
		}
		return movies;
	}

	public MovieDto getMovieForId(long id) throws SQLException {
		Connection databaseConnection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = databaseConnection.prepareStatement("select * from eyetracking_movie where pk_eyetracking_movie = ?");
		statement.setLong(1, id);
		ResultSet result = statement.executeQuery();
		List<MovieDto> movies = extractMovies(result);
		if (!movies.isEmpty()) {
			return movies.get(0);
		}

		return null;
	}

	public List<MovieDto> getMovieForPpmInstance(long id) throws SQLException {
		Connection databaseConnection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = databaseConnection.prepareStatement("select * from eyetracking_movie where process_instance = ?");
		statement.setLong(1, id);
		ResultSet result = statement.executeQuery();
		List<MovieDto> movies = extractMovies(result);
		cleanUp(databaseConnection, statement, result);
		return movies;
	}

	public List<MovieDto> getMoviesForSubject(Connection connection, long subjectId) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select * from eyetracking_movie where fk_subject = ?");
		statement.setLong(1, subjectId);
		ResultSet resultSet = statement.executeQuery();
		List<MovieDto> movies = extractMovies(resultSet);
		statement.close();
		return movies;
	}

	public void insertMovie(File movieFile, long userId, String fileName, String movieType, Long processInstance, long startTimestamp,
			long subject, long fileId) throws Exception {
		UserFileDao userFileDao = new UserFileDao();
		String moviePath = userFileDao.generateRelativeMoviePath(userId, fileName);
		String absoluteMovieFilePath = userFileDao.getAbsolutePath(moviePath);
		try (FileInputStream source = new FileInputStream(movieFile);
				FileOutputStream target = new FileOutputStream(new File(absoluteMovieFilePath))) {
			IOUtils.copyLarge(source, target);
		}

		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = connection.prepareStatement(
				"insert into eyetracking_movie (process_instance, movie_path, movie_type, start_timestamp, fk_subject, fk_user_file) values (?,?,?,?,?,?)");

		if (processInstance != null) {
			statement.setLong(1, processInstance);
		} else {
			statement.setNull(1, Types.BIGINT);
		}

		statement.setString(2, moviePath);
		statement.setString(3, movieType);
		statement.setLong(4, startTimestamp);
		statement.setLong(5, subject);
		statement.setLong(6, fileId);

		statement.executeUpdate();
		cleanUp(connection, statement);
	}

	public boolean isFileLinkedToMovie(Connection connection, long fileId) throws SQLException {
		PreparedStatement statement = connection
				.prepareStatement("select pk_eyetracking_movie from eyetracking_movie where fk_user_file=?;");
		statement.setLong(1, fileId);

		ResultSet result = statement.executeQuery();
		boolean isLinked = result.next();

		cleanUp(result, statement);

		return isLinked;
	}

	public boolean isMovieLinkedToPpmInstance(long processInstanceId) throws SQLException {
		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		PreparedStatement statement = connection.prepareStatement("select * from eyetracking_movie where process_instance=?");
		statement.setLong(1, processInstanceId);

		ResultSet result = statement.executeQuery();
		boolean isNotEmpty = result.next();
		cleanUp(connection, statement, result);
		return isNotEmpty;
	}
}
