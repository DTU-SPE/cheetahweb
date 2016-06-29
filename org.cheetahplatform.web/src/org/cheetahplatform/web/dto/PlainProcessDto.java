package org.cheetahplatform.web.dto;

public class PlainProcessDto {
	private long databaseId;
	private String id;
	private String data;
	private String type;
	private String notation;
	private long synchronizedFrom;

	public PlainProcessDto(long databaseId, String id, String data, String type, String notation, long synchronizedFrom) {
		this.databaseId = databaseId;
		this.id = id;
		this.data = data;
		this.type = type;
		this.notation = notation;
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

	public String getNotation() {
		return notation;
	}

	public long getSynchronizedFrom() {
		return synchronizedFrom;
	}

	public String getType() {
		return type;
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

	public void setNotation(String notation) {
		this.notation = notation;
	}

	public void setSynchronizedFrom(long synchronizedFrom) {
		this.synchronizedFrom = synchronizedFrom;
	}

	public void setType(String type) {
		this.type = type;
	}

}
