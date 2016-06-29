package org.cheetahplatform.web.dto;

public class SubjectDto {
	private long subjectId;
	private String subjectName;
	private String study;

	public SubjectDto() {
		// JSON
	}

	public SubjectDto(long subjectId, String subjectName, String study) {
		this.subjectId = subjectId;
		this.subjectName = subjectName;
		this.study = study;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof SubjectDto)) {
			return false;
		}

		SubjectDto other = (SubjectDto) obj;
		return subjectId == other.getSubjectId();
	}

	public String getStudy() {
		return study;
	}

	public long getSubjectId() {
		return subjectId;
	}

	public String getSubjectName() {
		return subjectName;
	}

	@Override
	public int hashCode() {
		return subjectName.hashCode();
	}

	public void setStudy(String study) {
		this.study = study;
	}

	public void setSubjectId(long subjectId) {
		this.subjectId = subjectId;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}
}
