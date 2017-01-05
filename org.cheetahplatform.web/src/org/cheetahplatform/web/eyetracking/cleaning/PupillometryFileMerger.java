package org.cheetahplatform.web.eyetracking.cleaning;

import java.io.IOException;
import java.util.List;

public class PupillometryFileMerger {
	private PupillometryFile pupillometryFile;
	private String separator;

	public PupillometryFileMerger(String separator) throws IOException {
		this.separator = separator;
	}

	private void addColumnsIfNecessary(PupillometryFile toAdd) throws IOException {
		PupillometryFileHeader header = toAdd.getHeader();
		List<PupillometryFileColumn> columns = header.getColumns();
		for (PupillometryFileColumn pupillometryFileColumn : columns) {
			String columnName = pupillometryFileColumn.getName();

			if (!pupillometryFile.hasColumn(columnName)) {
				pupillometryFile.appendColumn(columnName);
			}
		}
	}

	public void addFile(PupillometryFile toAdd) throws IOException {
		if (pupillometryFile == null) {
			pupillometryFile = new PupillometryFile(separator);
		}

		addColumnsIfNecessary(toAdd);
		appendLines(toAdd);
	}

	private void appendLines(PupillometryFile toAdd) throws IOException {
		List<PupillometryFileColumn> allColumns = pupillometryFile.getHeader().getColumns();

		for (PupillometryFileLine pupillometryFileLine : toAdd.getContent()) {
			PupillometryFileLine lineToAppend = pupillometryFile.appendLine();
			for (PupillometryFileColumn column : allColumns) {
				String value = "";
				if (toAdd.hasColumn(column.getName())) {
					value = pupillometryFileLine.get(column);
				}
				lineToAppend.setValue(column, value);
			}
		}
	}

	public PupillometryFile getPupillometryFile() {
		return pupillometryFile;
	}
}
