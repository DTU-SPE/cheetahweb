package org.cheetahplatform.web.dto;

public class InteractionDto {
	private int id;
	private long timestamp;
	private String workflowElement;
	private String type;
	private String typeToDisplay;
	private Long startTime;

	public InteractionDto(int id, long timestamp, String workflowElement, String type) {
		this(id, timestamp, workflowElement, type, null, null);
	}

	public InteractionDto(int id, long timestamp, String workflowElement, String type, String typeToDisplay, Long startTime) {
		this.id = id;
		this.timestamp = timestamp;
		this.workflowElement = workflowElement;
		this.type = type;
		this.typeToDisplay = typeToDisplay;
		this.startTime = startTime;
	}

	public int getId() {
		return id;
	}

	public Long getStartTime() {
		return startTime;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getType() {
		return type;
	}

	public String getTypeToDisplay() {
		return typeToDisplay;
	}

	public String getWorkflowElement() {
		return workflowElement;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setTypeToDisplay(String typeToDisplay) {
		this.typeToDisplay = typeToDisplay;
	}

	public void setWorkflowElement(String workflowElement) {
		this.workflowElement = workflowElement;
	}

}
