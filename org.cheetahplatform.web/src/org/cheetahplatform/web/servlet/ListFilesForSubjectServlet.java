package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.MovieDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.MovieDto;
import org.cheetahplatform.web.dto.UserFileDto;

public class ListFilesForSubjectServlet extends AbstractCheetahServlet {

	public static class ListFilesForSubjectRequest {
		private long subjectId;

		public long getSubjectId() {
			return subjectId;
		}

		public void setSubjectId(long subjectId) {
			this.subjectId = subjectId;
		}

	}

	public static class ListFilesForSubjectResponse {
		private List<UserFileDto> files;
		private List<MovieDto> movies;
		private List<UserFileDto> candidatesForConnecting;

		public ListFilesForSubjectResponse(List<UserFileDto> files, List<MovieDto> movies, List<UserFileDto> candidatesForConnecting) {
			this.files = files;
			this.movies = movies;
			this.candidatesForConnecting = candidatesForConnecting;
		}

		public List<UserFileDto> getCandidatesForConnecting() {
			return candidatesForConnecting;
		}

		public List<UserFileDto> getFiles() {
			return files;
		}

		public List<MovieDto> getMovies() {
			return movies;
		}

	}

	private static final long serialVersionUID = 6991846981166715853L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ListFilesForSubjectRequest listFilesRequest = readJson(request, ListFilesForSubjectRequest.class);
		UserFileDao userFileDao = new UserFileDao();
		List<UserFileDto> files = userFileDao.getUserFilesForSubject(connection, listFilesRequest.getSubjectId());

		// keep csv files only
		Iterator<UserFileDto> filesIterator = files.iterator();
		while (filesIterator.hasNext()) {
			UserFileDto fileDto = filesIterator.next();
			if (!fileDto.getFilename().endsWith(".csv")) {
				filesIterator.remove();
			}
		}

		MovieDao movieDao = new MovieDao();
		List<MovieDto> movies = movieDao.getMoviesForSubject(connection, listFilesRequest.getSubjectId());
		List<UserFileDto> connectCandidates = userFileDao.findConnectCandidates(connection, listFilesRequest.getSubjectId());
		writeJson(response, new ListFilesForSubjectResponse(files, movies, connectCandidates));
	}

}
