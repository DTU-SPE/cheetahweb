package org.cheetahplatform.web.dto;

public class StudyDto {
	private Long id;
	private String name;
	private String comment;
	private Long synchronizedFrom;

	public StudyDto() {
		// for mappers
	}

	public StudyDto(Long id, String name, String comment) {
		this.id = id;
		this.name = name;
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Long getSynchronizedFrom() {
		return synchronizedFrom;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSynchronizedFrom(Long synchronizedFrom) {
		this.synchronizedFrom = synchronizedFrom;
	}
}
