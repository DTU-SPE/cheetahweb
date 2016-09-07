package org.cheetahplatform.web.dto;

public class InteractionDto {
	private int id;
	private long timestamp;
	private String workflowElement;
	private String type;

	public InteractionDto(int id, long timestamp, String workflowElement, String type) {
		this.id = id;
		this.timestamp = timestamp;
		this.workflowElement = workflowElement;
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getType() {
		return type;
	}

	public String getWorkflowElement() {
		return workflowElement;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setWorkflowElement(String workflowElement) {
		this.workflowElement = workflowElement;
	}

}
