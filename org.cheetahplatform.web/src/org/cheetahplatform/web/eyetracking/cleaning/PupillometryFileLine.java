package org.cheetahplatform.web.eyetracking.cleaning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PupillometryFileLine implements IPupillometryFileLine {
	private List<String> content;
	private Map<String, Object> markings;
	private String decimalSeparator;

	public PupillometryFileLine(String decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
		content = new ArrayList<String>();
	}

	public void add(String toAdd) {
		content.add(toAdd);
	}

	/**
	 * Creates a copy of this line (omitting the markings).
	 *
	 * @return
	 */
	public PupillometryFileLine copy() {
		PupillometryFileLine copy = new PupillometryFileLine(decimalSeparator);
		copy.content = new ArrayList<>(content);
		return copy;
	}

	public void deleteValue(PupillometryFileColumn column) {
		setValue(column, "");
	}

	@Override
	public String get(int columnNumber) {
		return content.get(columnNumber);
	}

	@Override
	public String get(PupillometryFileColumn column) {
		return content.get(column.getColumnNumber());
	}

	public Double getDouble(PupillometryFileColumn column) {
		String value = get(column).replaceAll(",", ".");
		if (value == null || value.isEmpty()) {
			return null;
		}

		return Double.parseDouble(value);
	}

	public int getInteger(PupillometryFileColumn column) {
		String value = get(column);
		return Integer.parseInt(value);
	}

	public long getLong(PupillometryFileColumn column) {
		String value = get(column);
		return Long.parseLong(value);
	}

	public Object getMarking(String key) {
		if (markings == null) {
			return null;
		}

		return markings.get(key);
	}

	@Override
	public String getString(String separator) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;

		for (String entry : content) {
			if (!first) {
				builder.append(separator);
			}

			first = false;
			builder.append(entry);
		}
		return builder.toString();
	}

	public boolean isEmpty(PupillometryFileColumn column) {
		return get(column).trim().isEmpty();
	}

	public boolean isMarked(String marking) {
		if (markings == null) {
			return false;
		}

		return markings.containsKey(marking);
	}

	public void mark(String marking) {
		mark(marking, new Object());
	}

	public void mark(String marking, Object value) {
		if (this.markings == null) {
			this.markings = new HashMap<>();
		}

		this.markings.put(marking, value);
	}

	public void setValue(PupillometryFileColumn column, double value) {
		setValue(column, String.valueOf(value).replaceAll("\\.", decimalSeparator));
	}

	public void setValue(PupillometryFileColumn column, long value) {
		setValue(column, String.valueOf(value));
	}

	public void setValue(PupillometryFileColumn column, String value) {
		int columnNumber = column.getColumnNumber();
		content.set(columnNumber, value);
	}

	@Override
	public int size() {
		return content.size();
	}

	@Override
	public String toString() {
		return content.toString();
	}

	public void unmark(String marking) {
		markings.remove(marking);

		// make the gc's life easier
		if (markings.isEmpty()) {
			markings = null;
		}
	}
}
