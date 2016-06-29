package org.cheetahplatform.web.eyetracking.cleaning;

public class PupillometryFileColumn {
	private String name;
	private int columnNumber;

	public PupillometryFileColumn(String name, int columnNumber) {
		this.name = name;
		this.columnNumber = columnNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public String getName() {
		return name;
	}
}
