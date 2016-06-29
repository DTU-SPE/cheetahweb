package org.cheetahplatform.web.dto;

import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryParameter;

public class PupillometryFilterDto {
	private long id;
	private String name;
	private List<PupillometryParameter> parameters;

	public PupillometryFilterDto() {
		// JSON
	}

	public PupillometryFilterDto(long id, String name, List<PupillometryParameter> requiredColumns) {
		this.id = id;
		this.name = name;
		this.parameters = requiredColumns;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<PupillometryParameter> getParameters() {
		return parameters;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRequiredColumns(List<PupillometryParameter> requiredColumns) {
		this.parameters = requiredColumns;
	}
}
