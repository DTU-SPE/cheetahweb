package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.MovieDao;
import org.cheetahplatform.web.dto.MovieDto;

public class MovieServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 3927784862402996951L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		try {
			List<MovieDto> movies = null;
			String idParameter = request.getParameter("id");
			String processInstanceParameter = request.getParameter("processInstance");
			MovieDao movieDao = new MovieDao();

			if (idParameter != null) {
				long id = Long.parseLong(idParameter);
				movies = Arrays.asList(movieDao.getMovieForId(id));
			} else if (processInstanceParameter != null) {
				long processInstance = Long.parseLong(processInstanceParameter);
				movies = movieDao.getMovieForPpmInstance(processInstance);
			} else {
				throw new RuntimeException("Expected either parameter id or processInstance");
			}

			for (MovieDto movieDto : movies) {
				movieDto.removeWebAppsPrefix();
			}

			writeJson(response, movies);
		} catch (Exception e) {
			e.printStackTrace();
			writeJson(response, new ArrayList<>());
		}
	}
}
