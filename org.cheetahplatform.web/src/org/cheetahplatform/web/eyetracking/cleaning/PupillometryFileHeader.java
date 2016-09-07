package org.cheetahplatform.web.eyetracking.cleaning;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PupillometryFileHeader implements IPupillometryFileLine {
	private Map<String, PupillometryFileColumn> columns;

	public PupillometryFileHeader() {
		columns = new LinkedHashMap<String, PupillometryFileColumn>();
	}

	public PupillometryFileColumn appendColumn(String name) {
		if (columns.containsKey(name)) {
			throw new RuntimeException("Column " + name + " is already present");
		}

		PupillometryFileColumn column = new PupillometryFileColumn(name, columns.size());
		columns.put(name, column);
		return column;
	}

	@Override
	public String get(int columnNumber) {
		PupillometryFileColumn column = getColumns().get(columnNumber);
		if (column.getColumnNumber() != columnNumber) {
			throw new IllegalStateException("Illegal column index.");
		}

		return column.getName();
	}

	@Override
	public String get(PupillometryFileColumn column) {
		return column.getName();
	}

	public PupillometryFileColumn getColumn(String columnName) {
		return columns.get(columnName);
	}

	public List<PupillometryFileColumn> getColumns() {
		return new ArrayList<PupillometryFileColumn>(columns.values());
	}

	@Override
	public String getString(String separator) {
		StringBuilder builder = new StringBuilder();
		for (PupillometryFileColumn csvColumn : getColumns()) {
			if (builder.length() > 0) {
				builder.append(separator);
			}
			builder.append(csvColumn.getName());
		}
		return builder.toString();
	}

	public boolean hasColumn(String column) {
		return columns.containsKey(column);
	}

	@Override
	public int size() {
		return columns.size();
	}
}
