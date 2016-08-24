package org.cheetahplatform.web.servlet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cheetahplatform.web.dao.StudyDao;
import org.cheetahplatform.web.dao.SubjectDao;
import org.cheetahplatform.web.dto.CreateSubjectRequest;
import org.cheetahplatform.web.dto.CreateSubjectResponse;
import org.cheetahplatform.web.dto.StudyDto;

public class FileUploadServlet extends AbstractCheetahServlet {

	static class FileUploadRespone {
		private String message;
		private List<CreateSubjectResponse> subjectList;

		public FileUploadRespone(List<CreateSubjectResponse> subjectList) {
			this.message = null;
			this.subjectList = subjectList;
		}

		public FileUploadRespone(String message) {
			this.message = message;
			this.subjectList = null;
		}

		public String getMessage() {
			return message;
		}

		public List<CreateSubjectResponse> getSubjectList() {
			return subjectList;
		}

	}

	private static final long serialVersionUID = -3488611226776498100L;

	@Override
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		List<FileItem> file = null;
		List<CreateSubjectRequest> listOfNewSubjects = new ArrayList<>();
		List<StudyDto> studies = new StudyDao().getStudies(connection);
		SubjectDao subjectDao = new SubjectDao();
		List<CreateSubjectResponse> subjectResponseList = new ArrayList<>();

		try {
			file = upload.parseRequest(req);
		} catch (FileUploadException e) {
			throw new ServletException(e);
		}
		if (file == null || file.isEmpty()) {
			return;
		}
		FileItem fileItem = file.get(0);
		InputStream input = fileItem.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		try {
			makeCreateSubjectReequests(listOfNewSubjects, studies, reader);
		} catch (Exception e) {

			writeJson(resp, new FileUploadRespone(e.getMessage()));
			return;
		}
		for (CreateSubjectRequest createSubjecRequest : listOfNewSubjects) {
			if (subjectDao.subjectExists(connection, createSubjecRequest.getEmail())) {

				writeJson(resp, new FileUploadRespone(
						"There is already a subject with the email adress n" + createSubjecRequest.toString() + " in the database."));
				return;
			}
		}
		for (CreateSubjectRequest createSubjecRequest : listOfNewSubjects) {
			CreateSubjectResponse createSubject = subjectDao.createSubject(connection, createSubjecRequest);
			subjectResponseList.add(createSubject);

		}

		resp.setStatus(HttpServletResponse.SC_OK);
		writeJson(resp, new FileUploadRespone(subjectResponseList));

	}

	private void makeCreateSubjectReequests(List<CreateSubjectRequest> listOfNewSubjects, List<StudyDto> studies, BufferedReader reader)
			throws Exception {
		String line = reader.readLine();
		if (!line.trim().equals("email;study;subjectId;comment")) {
			throw new Exception("The first line in your CSV is not correct.\n The line should email;study;subjectId;comment ");
		}
		line = reader.readLine();
		while (line != null) {
			Long studyId = null;

			if (!(line.trim().equals(""))) {
				String[] array = line.split(";");
				if (array.length != 4) {
					throw new Exception("There is an error in the CSV.\n The wrong line is:\n" + line);
				}
				for (StudyDto study : studies) {
					if (study.getName().equals(array[1])) {
						studyId = study.getId();
						break;
					}
				}
				if (studyId == null) {
					throw new Exception("The study  \"" + array[1] + "\" was not found in the database.");
				}
				CreateSubjectRequest createSubjectRequest = new CreateSubjectRequest(false);
				createSubjectRequest.setEmail(array[0]);
				createSubjectRequest.setStudyId(studyId);
				createSubjectRequest.setSubjectId(array[2]);
				createSubjectRequest.setComment(array[3]);
				listOfNewSubjects.add(createSubjectRequest);
			} else {
				throw new Exception("The CSV has empty lines.\nPlease remove them.");
			}
			line = reader.readLine();
		}
	}

}
