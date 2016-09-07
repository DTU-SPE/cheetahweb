package org.cheetahplatform.web.dto;

import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;

public class StudyDto {
	private Long id;
	private String name;
	private String comment;
	private Long synchronizedFrom;
	private List<DataProcessing> dataProcessing;

	public StudyDto() {
		dataProcessing = new ArrayList<>();
	}

	public StudyDto(Long id, String name, String comment) {
		this();

		this.id = id;
		this.name = name;
		this.comment = comment;
	}

	public void addDataProcessing(DataProcessing toAdd) {
		this.dataProcessing.add(toAdd);
	}

	public String getComment() {
		return comment;
	}

	public List<DataProcessing> getDataProcessing() {
		return dataProcessing;
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
