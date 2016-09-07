package org.cheetahplatform.web.dto;

public class ChangeSubjectRequest {
	Integer id;
	String email;
	String subjectId;
	String comment;

	public ChangeSubjectRequest() {
		super();
	}

	public ChangeSubjectRequest(Integer id, String email, String subjectId, String comment) {
		super();
		this.id = id;
		this.email = email;
		this.subjectId = subjectId;
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public String getEmail() {
		return email;
	}

	public Integer getId() {
		return id;
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

	public void setId(Integer id) {
		this.id = id;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

}
