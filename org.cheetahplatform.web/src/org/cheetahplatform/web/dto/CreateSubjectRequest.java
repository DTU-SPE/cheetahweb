package org.cheetahplatform.web.dto;

public class CreateSubjectRequest {
	private String email;
	private String subjectId;
	private long studyId;
	private String comment;
	private boolean allowDouble;

	public CreateSubjectRequest() {

	}

	public CreateSubjectRequest(boolean allowDouble) {
		this.allowDouble = allowDouble;
	}

	public boolean getAllowDouble() {
		return allowDouble;
	}

	public String getComment() {
		return comment;
	}

	public String getEmail() {
		return email;
	}

	public long getStudyId() {
		return studyId;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setAllowDouble(Boolean allowDouble) {
		this.allowDouble = allowDouble;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setStudyId(long studyId) {
		this.studyId = studyId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	@Override
	public String toString() {
		return "email=" + email + ", subjectId=" + subjectId + ", studyId=" + studyId + ", comment=" + comment;
	}

}
