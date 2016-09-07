package org.cheetahplatform.web.dto;

public class CreateSubjectResponse {
	private String error;
	private long id;
	private String email;
	private String subjectId;
	private long studyId;
	private String comment;

	public CreateSubjectResponse(long id, String email, String subjectId, long studyId, String comment) {
		this.error = null;
		this.id = id;
		this.subjectId = subjectId;
		this.studyId = studyId;
		this.comment = comment;
		this.email = email;
	}

	public CreateSubjectResponse(String error) {
		this.error = error;
	}

	public String getComment() {
		return comment;
	}

	public String getEmail() {
		return email;
	}

	public String getError() {
		return error;
	}

	public long getId() {
		return id;
	}

	public long getStudyId() {
		return studyId;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setStudyId(long studyId) {
		this.studyId = studyId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

}
