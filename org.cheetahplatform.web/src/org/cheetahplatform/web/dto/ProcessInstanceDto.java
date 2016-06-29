package org.cheetahplatform.web.dto;

public class ProcessInstanceDto {
	private long id;
	private String experiment;
	private String notation;
	private String model;

	public ProcessInstanceDto(long id, String experiment, String notation, String model) {
		this.id = id;
		this.experiment = experiment;
		this.notation = notation;
		this.model = model;
	}

	public long getId() {
		return id;
	}

	public String getExperiment() {
		return experiment;
	}

	public String getNotation() {
		return notation;
	}

	public String getModel() {
		return model;
	}

}
