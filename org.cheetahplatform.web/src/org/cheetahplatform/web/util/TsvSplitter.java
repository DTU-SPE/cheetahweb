package org.cheetahplatform.web.util;

import static org.cheetahplatform.web.eyetracking.DatabaseEyeTrackingSource.split;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TsvSplitter {
	private static final String SEPARATOR = "@";
	private static final String PARTICIPANT_NAME = "ParticipantName";
	private static final String PROCESS = "task_modeling_1.0";

	public static void main(String[] args) throws IOException {
		File inputFile = new File("F:\\StudieManuel\\The Influence of WMC & EF on BPM_Data_Export.tsv");
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String header = reader.readLine();
		List<String> headerColumns = split(header, '\t');

		int participantIndex = -1;
		for (int i = 0; i < headerColumns.size(); i++) {
			String column = headerColumns.get(i);
			if (column.equals(PARTICIPANT_NAME)) {
				participantIndex = i;
			}
		}

		if (participantIndex < 0) {
			reader.close();
			throw new IllegalArgumentException("Could not find participant column '" + PARTICIPANT_NAME + "'.");
		}

		File outputFile;
		FileWriter writer = null;

		String participant = "";
		String line = reader.readLine();
		while (line != null) {
			List<String> token = split(line, '\t');
			String newParticipant = token.get(participantIndex);
			if (!newParticipant.equals(participant)) {
				String fileName = newParticipant + SEPARATOR + PROCESS + ".tsv";
				System.out.println("New Participant '" + newParticipant + "'. Creating file '" + fileName + "'.");
				if (writer != null) {
					writer.close();
				}
				participant = newParticipant;
				outputFile = new File(inputFile.getParentFile(), fileName);
				writer = new FileWriter(outputFile);
				writer.write(header);
				writer.write("\n");
			}
			writer.write(line);
			writer.write("\n");
			line = reader.readLine();
		}

		reader.close();
		if (writer != null) {
			writer.close();
		}
	}
}
