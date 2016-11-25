package org.cheetahplatform.web.eyetracking.cleaning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.input.BOMInputStream;

public class PupillometryFile {
	public static final String SCENE = "Scene";
	public static final String SEPARATOR_SEMICOLON = ";";
	public static final String SEPARATOR_TABULATOR = "\t";
	public static final String SEPARATOR_COMMA = ",";
	public static final String COLLAPSED_COLUMNS = "cheetah_internal_collapsed_columns";

	public static List<PupillometryFileLine> extractContent(BufferedReader reader, String decimalSeparator, String separator)
			throws IOException {
		return extractContent(reader, decimalSeparator, separator, Integer.MAX_VALUE);
	}

	public static List<PupillometryFileLine> extractContent(BufferedReader reader, String decimalSeparator, String separator, int limit)
			throws IOException {
		List<PupillometryFileLine> content = new ArrayList<PupillometryFileLine>();
		String line = reader.readLine();
		while (line != null) {
			PupillometryFileLine currentLine = new PupillometryFileLine(decimalSeparator);
			String[] token = line.split(separator, -1);
			for (String string : token) {
				currentLine.add(string.intern());
			}
			content.add(currentLine);
			if (content.size() >= limit) {
				break;
			}

			line = reader.readLine();
		}

		return content;
	}

	public static PupillometryFileHeader extractHeader(BufferedReader reader, String separator) throws IOException {
		String line = reader.readLine();
		PupillometryFileHeader header = new PupillometryFileHeader();
		String[] token = line.split(separator, -1);
		for (String string : token) {
			header.appendColumn(string.intern());
		}

		return header;
	}

	private InputStream input;
	private List<PupillometryFileLine> content;
	private PupillometryFileHeader header;
	private String separator;

	private boolean hasHeader;

	private String decimalSeparator;

	public PupillometryFile(File file, String decimalSeparator) throws FileNotFoundException {
		this(new BOMInputStream(new FileInputStream(file)), decimalSeparator);
	}

	public PupillometryFile(File file, String separator, boolean hasHeader, String decimalSeparator) throws FileNotFoundException {
		this(new BOMInputStream(new FileInputStream(file)), decimalSeparator);
		this.separator = separator;
		this.hasHeader = hasHeader;
		this.decimalSeparator = decimalSeparator;
	}

	public PupillometryFile(InputStream input, String decimalSeparator) {
		this(input, SEPARATOR_TABULATOR, false);
	}

	public PupillometryFile(InputStream input, String separatorToken, boolean hasHeader) {
		this.separator = separatorToken;
		this.input = input;
		this.hasHeader = hasHeader;
		this.decimalSeparator = SEPARATOR_COMMA;
	}

	public PupillometryFile(String decimalSeparator) throws IOException {
		this(new ByteArrayInputStream("".getBytes()), decimalSeparator);

		header = new PupillometryFileHeader();
		content = new LinkedList<>();
	}

	/**
	 * Some data sources provide the time stamps as ms, some as microseconds - adapt all values to microseconds.
	 *
	 * @param column
	 * @throws IOException
	 */
	public void adaptTimestamps(PupillometryFileColumn column) throws IOException {
		if (content.size() < 2) {
			return;
		}

		LinkedList<PupillometryFileLine> content = getContent();
		double firstTimestamp = content.get(0).getDouble(column);
		double secondTimestamp = content.get(1).getDouble(column);
		if (secondTimestamp - firstTimestamp > 100) {
			return; // already microseconds
		}

		for (PupillometryFileLine line : content) {
			if (line.isEmpty(column)) {
				continue;
			}

			long timestamp = (long) (line.getDouble(column) * 1000);
			line.setValue(column, timestamp);
		}
	}

	/**
	 * Appends a new column and ensures that data is available for this column.
	 *
	 * @param columnName
	 * @return
	 * @throws IOException
	 */
	public PupillometryFileColumn appendColumn(String columnName) throws IOException {
		PupillometryFileColumn column = header.appendColumn(columnName);

		// fill the new column with empty values
		for (PupillometryFileLine line : getContent()) {
			line.add("");
		}

		return column;
	}

	public PupillometryFileLine appendLine() {
		PupillometryFileLine newLine = new PupillometryFileLine(decimalSeparator);
		// fill the new column with empty values
		for (int i = 0; i < header.size(); i++) {
			newLine.add("");
		}

		content.add(newLine);
		return newLine;
	}

	public void appendLine(PupillometryFileLine toAppend) {
		if (toAppend.size() != header.getColumns().size()) {
			throw new RuntimeException("Cannot append the given column, as it contains a different numbers of columns.");
		}

		content.add(toAppend.copy());
	}

	/**
	 * Tobii export creates empty columns when there is data for "StudioEvent" and "StudioEventData". This method removes these rows and
	 * adds the removed rows to the previous row.
	 *
	 * @param timestampColumn
	 *            the column that identifies timestamps
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void collapseEmptyColumns(PupillometryFileColumn timestampColumn) throws IOException {
		if (content == null) {
			read();
		}

		Iterator<PupillometryFileLine> iterator = content.iterator();
		PupillometryFileLine lastRowWithTimestamp = null; // add the marking to the previous column (assumption: there will never be an
		// empty row at the beginning of the file)
		while (iterator.hasNext()) {
			PupillometryFileLine current = iterator.next();
			String timestampValue = current.get(timestampColumn);

			if (timestampValue.trim().isEmpty()) {
				List<PupillometryFileLine> collapsed = (List<PupillometryFileLine>) lastRowWithTimestamp.getMarking(COLLAPSED_COLUMNS);
				if (collapsed == null) {
					collapsed = new LinkedList<>();
					lastRowWithTimestamp.mark(COLLAPSED_COLUMNS, collapsed);
				}

				collapsed.add(current);
				iterator.remove();
			} else {
				lastRowWithTimestamp = current;
			}
		}
	}

	/**
	 * Copies a column.
	 *
	 * @param toCopy
	 *            the column to be copied
	 * @param target
	 *            the target column (will be created)
	 * @throws IOException
	 */
	public void copyColumn(PupillometryFileColumn toCopy, String target) throws IOException {
		if (content == null) {
			read();
		}

		PupillometryFileColumn targetColumn = appendColumn(target);
		for (PupillometryFileLine line : getContent()) {
			String valueToCopy = line.get(toCopy);
			line.setValue(targetColumn, valueToCopy);
		}
	}

	/**
	 * Returns a copy of this file with headers only; omits the content.
	 *
	 * @return
	 * @throws IOException
	 */
	public PupillometryFile emptyCopy() throws IOException {
		PupillometryFile copy = new PupillometryFile(decimalSeparator);
		copy.separator = separator;
		for (PupillometryFileColumn column : getHeader().getColumns()) {
			copy.appendColumn(column.getName());
		}

		return copy;
	}

	public List<String> getColumn(int columnNumber) throws IOException {
		if (content == null) {
			read();
		}

		List<String> column = new ArrayList<String>();
		for (IPupillometryFileLine line : content) {
			column.add(line.get(columnNumber));
		}

		return column;
	}

	public int getColumnCount() throws IOException {
		if (content == null) {
			read();
		}

		if (content.isEmpty()) {
			return 0;
		}

		return content.get(0).size();
	}

	public LinkedList<PupillometryFileLine> getContent() throws IOException {
		if (content == null) {
			read();
		}

		return new LinkedList<>(content);
	}

	public PupillometryFileHeader getHeader() throws IOException {
		if (hasHeader && header == null) {
			read();
		}

		return header;
	}

	public Iterator<PupillometryFileLine> getIteratorStartingAt(long timestamp, PupillometryFileColumn timestampColumn) {
		int index = 0;
		for (PupillometryFileLine line : content) {
			long currentTimestamp = line.getLong(timestampColumn);
			if (currentTimestamp > timestamp) {
				break;
			}
			index++;
		}

		return content.listIterator(index);
	}

	public List<IPupillometryFileLine> getLines() {
		List<IPupillometryFileLine> lines = new LinkedList<IPupillometryFileLine>();
		lines.add(header);
		lines.addAll(content);
		return lines;
	}

	public boolean hasColumn(String column) {
		return header.hasColumn(column);
	}

	/**
	 * Copies the current scene to a separate column.
	 *
	 * @param contributor
	 *
	 * @throws IOException
	 */
	public void processSceneColumns(IAnalysisContributor contributor) throws IOException {
		if (content == null) {
			read();
		}

		PupillometryFileColumn sceneColumn = appendColumn(SCENE);
		PupillometryFileColumn eventColumn = header.getColumn("StudioEvent");
		PupillometryFileColumn eventDataColumn = header.getColumn("StudioEventData");
		contributor.processSceneColumns(content, sceneColumn, eventColumn, eventDataColumn);
	}

	private List<IPupillometryFileLine> read() throws IOException {
		if (content != null) {
			return getLines();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		if (hasHeader) {
			header = extractHeader(reader, separator);
		}
		content = extractContent(reader, decimalSeparator, separator);

		reader.close();
		input = null;
		return getLines();
	}

	public boolean removeLine(IPupillometryFileLine pupillometryFileLine) {
		return content.remove(pupillometryFileLine);
	}

	/**
	 * Remove all null values, indicated by nullValueMask.
	 *
	 * @param nullValueMask
	 * @throws IOException
	 */
	public void removeNullValues(String nullValueMask) throws IOException {
		for (PupillometryFileLine line : getContent()) {
			for (PupillometryFileColumn column : header.getColumns()) {
				String value = line.get(column);

				if (value.equals(nullValueMask)) {
					line.setValue(column, "");
				}
			}
		}
	}

	public void writeToFile(File file) throws IOException {
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
		List<IPupillometryFileLine> lines = getLines();
		for (IPupillometryFileLine line : lines) {
			String string = line.getString(separator);
			fileWriter.write(string + "\n");
		}

		fileWriter.close();
	}

}
