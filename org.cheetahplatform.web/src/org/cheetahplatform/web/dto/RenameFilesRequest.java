package org.cheetahplatform.web.dto;

import java.util.Map;

public class RenameFilesRequest {
	private Map<Long, String> files;

	public void addFile(long id, String newName) {
		files.put(id, newName);
	}

	public Map<Long, String> getFiles() {
		return files;
	}

	public void setFiles(Map<Long, String> files) {
		this.files = files;
	}
}
