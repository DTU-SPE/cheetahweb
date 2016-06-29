package org.cheetahplatform.web.dto;

public class PlainSubjectDto {
	private long id;
	private String email;
	private String subjectId;
	private long study;
	private String comment;
	private long synchronizedFrom;

	public PlainSubjectDto(long id, String email, String subjectId, long study, String comment, long synchronizedFrom) {
		this.id = id;
		this.email = email;
		this.subjectId = subjectId;
		this.study = study;
		this.comment = comment;
		this.synchronizedFrom = synchronizedFrom;
	}

	public String getComment() {
		return comment;
	}

	public String getEmail() {
		return email;
	}

	public long getId() {
		return id;
	}

	public long getStudy() {
		return study;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public long getSynchronizedFrom() {
		return synchronizedFrom;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setStudy(long study) {
		this.study = study;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public void setSynchronizedFrom(long synchronizedFrom) {
		this.synchronizedFrom = synchronizedFrom;
	}
}
