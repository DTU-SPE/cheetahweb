package org.cheetahplatform.web.dto;

public class PlainProcessInstanceDto {
	private long databaseId;
	private long process;
	private String id;
	private String data;
	private long subject;
	private long synchronizedFrom;

	public PlainProcessInstanceDto(long databaseId, long process, String id, String data, long subject, long synchronizedFrom) {
		this.databaseId = databaseId;
		this.process = process;
		this.id = id;
		this.data = data;
		this.subject = subject;
		this.synchronizedFrom = synchronizedFrom;
	}

	public String getData() {
		return data;
	}

	public long getDatabaseId() {
		return databaseId;
	}

	public String getId() {
		return id;
	}

	public long getProcess() {
		return process;
	}

	public long getSubject() {
		return subject;
	}

	public long getSynchronizedFrom() {
		return synchronizedFrom;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setDatabaseId(long databaseId) {
		this.databaseId = databaseId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setProcess(long process) {
		this.process = process;
	}

	public void setSubject(long subject) {
		this.subject = subject;
	}

	public void setSynchronizedFrom(long synchronizedFrom) {
		this.synchronizedFrom = synchronizedFrom;
	}
}
