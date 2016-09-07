package org.cheetahplatform.web.dto;

import java.util.Date;

public class PlainAuditTrailEntryDto {
	private long id;
	private long processInstance;
	private Date timestamp;
	private String type;
	private String workflowElement;
	private String originator;
	private String data;

	public PlainAuditTrailEntryDto(long id, long processInstance, Date timestamp, String type, String workflowElement, String originator,
			String data) {
		this.id = id;
		this.processInstance = processInstance;
		this.timestamp = timestamp;
		this.type = type;
		this.workflowElement = workflowElement;
		this.originator = originator;
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public long getId() {
		return id;
	}

	public String getOriginator() {
		return originator;
	}

	public long getProcessInstance() {
		return processInstance;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getType() {
		return type;
	}

	public String getWorkflowElement() {
		return workflowElement;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setOriginator(String originiator) {
		this.originator = originiator;
	}

	public void setProcessInstance(long processInstance) {
		this.processInstance = processInstance;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setWorkflowElement(String workflowElement) {
		this.workflowElement = workflowElement;
	}
}
