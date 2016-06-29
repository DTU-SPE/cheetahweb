package org.cheetahplatform.web.dto;

public class AddStudyRequest {
	private String name;
	private String comment;

	public AddStudyRequest() {
		// JSON
	}

	public String getComment() {
		return comment;
	}

	public String getName() {
		return name;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setName(String name) {
		this.name = name;
	}
}
