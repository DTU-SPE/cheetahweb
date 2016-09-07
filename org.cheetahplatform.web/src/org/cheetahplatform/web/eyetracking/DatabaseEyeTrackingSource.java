package org.cheetahplatform.web.eyetracking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.UserFileDto;

public class DatabaseEyeTrackingSource implements IEyeTrackingDataSource {

	private static final String EYE_TRACKER_TIMESTAMP = "EyeTrackerTimestamp";
	private static final String PUPIL_RIGHT = "PupilRight";
	private static final String PUPIL_LEFT = "PupilLeft";

	public static double parseDouble(String toParse) {
		try {
			return Double.parseDouble(toParse);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static List<String> split(String toSplit) {
		return split(toSplit, ';');
	}

	public static List<String> split(String toSplit, char splitChar) {
		int index = -1;
		List<String> tmp = new ArrayList<String>();

		for (int i = 0; i < toSplit.length(); i++) {
			if (toSplit.charAt(i) == splitChar) {
				String value = toSplit.substring(index + 1, i);
				tmp.add(value);
				index = i;
			}
		}

		if (toSplit.charAt(toSplit.length() - 1) != splitChar) {
			tmp.add(toSplit.substring(index + 1, toSplit.length()));
		} else {
			tmp.add("");
		}

		return tmp;
	}

	@Override
	public CachedEyeTrackingData load(long userFileId) throws Exception {
		UserFileDao userFileDao = new UserFileDao();
		UserFileDto file = userFileDao.getFile(userFileId);

		String path = userFileDao.getPath(file.getId());
		String absolutePath = userFileDao.getAbsolutePath(path);

		BufferedReader reader = new BufferedReader(new FileReader(new File(absolutePath)));
		String header = reader.readLine();
		List<String> headerColumns = split(header);

		int leftIndex = 0;
		int rightIndex = 0;
		int timestampIndex = 0;

		for (int i = 0; i < headerColumns.size(); i++) {
			String column = headerColumns.get(i);
			if (column.equals(PUPIL_LEFT)) {
				leftIndex = i;
			} else if (column.equals(PUPIL_RIGHT)) {
				rightIndex = i;
			} else if (column.equals(EYE_TRACKER_TIMESTAMP)) {
				timestampIndex = i;
			}
		}

		String line = reader.readLine();
		CachedEyeTrackingData data = new CachedEyeTrackingData(file.getFilename(), file.getId());
		while (line != null) {
			List<String> lineTokens = split(line);
			double left = parseDouble(leftIndex, lineTokens);
			double right = parseDouble(rightIndex, lineTokens);
			long timestamp = parseLong(timestampIndex, lineTokens);
			data.addEntry(new EyeTrackingEntry(timestamp, left, right));
			line = reader.readLine();
		}

		data.computeCachedValues();
		reader.close();
		return data;
	}

	private double parseDouble(int leftIndex, List<String> lineTokens) {
		String toParse = lineTokens.get(leftIndex);

		return parseDouble(toParse);
	}

	private long parseLong(int timestampIndex, List<String> lineTokens) {
		try {
			return Long.parseLong(lineTokens.get(timestampIndex));
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
