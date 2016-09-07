package org.cheetahplatform.web.dto;

public class UserFileTagDto {
	private long id;
	private String tag;

	public UserFileTagDto(long id, String tag) {
		this.id = id;
		this.tag = tag;
	}

	public long getId() {
		return id;
	}

	public String getTag() {
		return tag;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}
