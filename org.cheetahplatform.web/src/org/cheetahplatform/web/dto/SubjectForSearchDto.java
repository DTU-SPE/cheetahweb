package org.cheetahplatform.web.dto;

public class SubjectForSearchDto {
	private long id;
	private String email;
	private String subjectId;
	private StudyDto study;
	private String comment;

	public SubjectForSearchDto(long id, String email, String subjectId, StudyDto study, String comment) {
		this.id = id;
		this.email = email;
		this.subjectId = subjectId;
		this.study = study;
		this.comment = comment;
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

	public StudyDto getStudy() {
		return study;
	}

	public String getSubjectId() {
		return subjectId;
	}

}
