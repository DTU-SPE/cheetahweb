package org.cheetahplatform.web.dto;

import java.util.List;

import org.apache.commons.fileupload.FileItem;

public class PhaseCalculationCSV {
	private FileItem file;
	private List<Long> fileIds;

	public FileItem getFile() {
		return file;
	}

	public List<Long> getFileIds() {
		return fileIds;
	}

	public void setFile(FileItem file) {
		this.file = file;
	}

	public void setFileId(List<Long> fileIds) {
		this.fileIds = fileIds;
	}

	@Override
	public String toString() {
		return "PhaseCalculationCSV [file=" + file + ", fileId=" + fileIds + "]";
	}

}
