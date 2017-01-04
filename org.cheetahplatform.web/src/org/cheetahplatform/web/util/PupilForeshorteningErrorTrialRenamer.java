package org.cheetahplatform.web.util;

import java.io.File;
import java.util.List;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

/**
 * Small utility class for renaming the StudioEventData of the experiment to evaluate the pupil foreshortening error for Tobii TX300.
 *
 * @author Jakob
 */
public class PupilForeshorteningErrorTrialRenamer {
	private static final String STIMULUS = "Stimulus";
	private static final String TRIAL_START = "TrialStart";
	private static final String STUDIO_EVENT_DATA_COLUMN = "StudioEventData";
	public static final String FILE = "F:\\PupilForeshorteningError\\Forshortening_artificial_light_selected_12122016.tsv";

	public static void main(String[] args) throws Exception {
		File file = new File(FILE);

		System.out.println("Loading file...");
		PupillometryFile pupillometryFile = new PupillometryFile(file, PupillometryFile.SEPARATOR_TABULATOR, true,
				PupillometryFile.SEPARATOR_COMMA);
		PupillometryFileColumn column = pupillometryFile.getHeader().getColumn(STUDIO_EVENT_DATA_COLUMN);
		System.out.println("Processing file...");
		List<PupillometryFileLine> lines = pupillometryFile.getContent();

		String currentScene = "";
		int count = 0;

		for (PupillometryFileLine line : lines) {
			String value = line.get(column);
			if (value == null || value.trim().isEmpty()) {
				continue;
			}

			String trimmed = value.trim();
			if (!currentScene.equals(trimmed)) {
				currentScene = trimmed;
				count = 1;
				line.setValue(column, TRIAL_START);
			} else {
				count++;
				if (count >= 3) {
					line.setValue(column, STIMULUS);
				} else {
					line.setValue(column, TRIAL_START);
				}
			}
		}

		int separatorIndex = FILE.lastIndexOf(".");
		String prefix = FILE.substring(0, separatorIndex);
		String newFileName = prefix + "_trials_renamed" + FILE.substring(separatorIndex);
		System.out.println("Writing output to '" + newFileName + "'...");
		pupillometryFile.writeToFile(new File(newFileName));
		System.out.println("Done");
	}
}
