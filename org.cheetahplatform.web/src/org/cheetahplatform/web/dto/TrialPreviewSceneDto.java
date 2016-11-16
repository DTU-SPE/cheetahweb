package org.cheetahplatform.web.dto;

public class TrialPreviewSceneDto {
	private String type;
	private String name;

	public TrialPreviewSceneDto(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
}