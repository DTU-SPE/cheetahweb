package org.cheetahplatform.web.util;

import static org.cheetahplatform.web.eyetracking.DatabaseEyeTrackingSource.parseDouble;
import static org.cheetahplatform.web.eyetracking.DatabaseEyeTrackingSource.split;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class EyeTrackingCsvCleaner {

	public static void main(String[] args) throws IOException {
		File inputFile = new File("C:\\Users\\Jakob\\Desktop\\to_minimize.tsv");
		File outputFile = new File(inputFile.getParentFile(), inputFile.getName().replace("tsv", "csv"));

		FileWriter writer = new FileWriter(outputFile);
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String header = reader.readLine();
		List<String> headerColumns = split(header, '\t');

		int leftIndex = 0;
		int rightIndex = 0;
		int timestampIndex = 0;

		for (int i = 0; i < headerColumns.size(); i++) {
			String column = headerColumns.get(i);
			if (column.equals("PupilLeft")) {
				leftIndex = i;
			} else if (column.equals("PupilRight")) {
				rightIndex = i;
			} else if (column.equals("EyeTrackerTimestamp")) {
				timestampIndex = i;
			}
		}

		writer.write("EyeTrackerTimestamp;PupilLeft;PupilRight\n");

		String line = reader.readLine();
		while (line != null) {
			List<String> token = split(line, '\t');

			String timestamp = token.get(timestampIndex);
			double left = parseDouble(token.get(leftIndex).replaceAll(",", "."));
			double right = parseDouble(token.get(rightIndex).replaceAll(",", "."));

			String outputLine = timestamp + ";" + left + ";" + right + "\n";
			writer.write(outputLine);
			line = reader.readLine();
		}

		reader.close();
		writer.close();
	}
}
