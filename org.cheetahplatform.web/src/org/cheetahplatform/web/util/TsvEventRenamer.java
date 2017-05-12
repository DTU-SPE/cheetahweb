package org.cheetahplatform.web.util;

import static org.cheetahplatform.web.eyetracking.DatabaseEyeTrackingSource.split;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TsvEventRenamer {
	private static final String FOLDER = "D:\\tmp\\Memory Reactivation Task (MRT)\\Control\\Lösung2";
	private static final String OUTPUT_FOLDER = "D:\\tmp\\processed_MRT\\Lösung2";
	private static final String STUDIO_EVENT_DATA = "StudioEventData";

	private static String[] STIMULUS_MARKERS = new String[] { "Intro+NEUTRAL.wmv", "Intro+STRESS_.wmv", "IPT", "MRT", "RT_", "RT5NEU",
			"Q" };

	private static boolean isStimulus(String eventData) {
		for (String marker : STIMULUS_MARKERS) {
			if (eventData.startsWith(marker)) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		File root = new File(FOLDER);
		if (!root.isDirectory()) {
			System.err.println("This is not a directory! Exit.");
			return;
		}

		File[] files = root.listFiles();
		for (File inputFile : files) {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String header = reader.readLine();
			List<String> headerColumns = split(header, '\t');
			int eventDataIndex = -1;
			for (int i = 0; i < headerColumns.size(); i++) {
				String column = headerColumns.get(i);
				if (column.equals(STUDIO_EVENT_DATA)) {
					eventDataIndex = i;
				}
			}

			if (eventDataIndex < 0) {
				reader.close();
				throw new IllegalArgumentException("Could not find column '" + STUDIO_EVENT_DATA + "'.");
			}

			String fileName = inputFile.getName();
			System.out.println("Writing file " + fileName);
			File outputFile = new File(OUTPUT_FOLDER, fileName);
			FileWriter writer = new FileWriter(outputFile);
			writer.write(header);
			writer.write("\n");

			String line = reader.readLine();

			int count = 0;
			while (line != null) {
				List<String> token = split(line, '\t');
				String eventData = token.get(eventDataIndex);

				if ("fixation.jpg".equals(eventData)) {
					count++;
					String newName = null;
					if (count < 3) {
						newName = "fixation1";
					} else if (count >= 3) {
						newName = "fixation2";
					}
					if (count == 4) {
						count = 0;
					}

					if (newName == null) {
						reader.close();
						writer.close();
						throw new IllegalStateException("Cannot be null!");
					}
					line = line.replace("fixation.jpg", newName);
					System.out.println(line);
				}

				if (isStimulus(eventData)) {
					line = line.replace(eventData, "Stimulus");
					System.out.println(line);
				}

				writer.write(line);
				writer.write("\n");
				line = reader.readLine();
			}

			reader.close();
			writer.close();
		}
	}

	public static int renameFixation(BufferedReader reader, FileWriter writer, String line, int count, String eventData)
			throws IOException {
		if ("fixation.jpg".equals(eventData)) {
			count++;
			String newName = null;
			if (count < 3) {
				newName = "fixation1";
			} else if (count >= 3) {
				newName = "fixation2";
			}
			if (count == 4) {
				count = 0;
			}

			if (newName == null) {
				reader.close();
				writer.close();
				throw new IllegalStateException("Cannot be null!");
			}
			line = line.replace("fixation.jpg", newName);
			System.out.println(line);
		}

		return count;
	}
}
