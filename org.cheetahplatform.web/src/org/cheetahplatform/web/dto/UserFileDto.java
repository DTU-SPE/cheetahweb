package org.cheetahplatform.web.dto;

import java.util.ArrayList;
import java.util.List;

public class UserFileDto {
	private long id;
	private String filename;
	private String type;
	private String url;
	private String comment;
	private List<UserFileTagDto> tags;
	private long processInstanceId;
	private String processInstanceName;
	private Long subjectId;

	public UserFileDto() {
		// JSON
		tags = new ArrayList<>();
		processInstanceId = -1;
	}

	public UserFileDto(long id, String filename, String type, String url, String comment, Long subjectId) {
		this();

		this.id = id;
		this.filename = filename;
		this.type = type;
		this.url = url;
		this.comment = comment;
		this.subjectId = subjectId;
	}

	public void addTag(UserFileTagDto tag) {
		tags.add(tag);
	}

	public String getComment() {
		return comment;
	}

	public String getFilename() {
		return filename;
	}

	public long getId() {
		return id;
	}

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public String getProcessInstanceName() {
		return processInstanceName;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public List<UserFileTagDto> getTags() {
		return tags;
	}

	public String getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

	/**
	 * Extracts the actual filename used to store the file on the disk. This is different from the filename as the filename can also include
	 * spaces etc.
	 *
	 * @return the filename to store the file on the disk.
	 */
	public String getUrlFileName() {
		int lastIndex = url.lastIndexOf("/");
		return url.substring(lastIndex + 1);
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public void setProcessInstanceName(String processInstanceName) {
		this.processInstanceName = processInstanceName;
	}

	public void setTags(List<UserFileTagDto> tags) {
		this.tags = tags;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
