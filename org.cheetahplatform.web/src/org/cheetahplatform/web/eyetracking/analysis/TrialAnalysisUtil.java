package org.cheetahplatform.web.eyetracking.analysis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

public class TrialAnalysisUtil {

	@SuppressWarnings("unchecked")
	public static Set<String> extractKeysConsideringCollapsedColumns(IPupillometryFileLine line, PupillometryFileColumn column) {
		Set<String> foundKeys = null;
		String string = line.get(column);
		foundKeys = TrialAnalysisUtil.testKey(string, foundKeys);
		PupillometryFileLine casted = (PupillometryFileLine) line;
		List<PupillometryFileLine> collapsedLines = (List<PupillometryFileLine>) casted.getMarking(PupillometryFile.COLLAPSED_COLUMNS);

		if (collapsedLines != null) {
			for (IPupillometryFileLine collapsedLine : collapsedLines) {
				String currentScene = collapsedLine.get(column);
				foundKeys = TrialAnalysisUtil.testKey(currentScene, foundKeys);
			}
		}

		return foundKeys;
	}

	private static Set<String> testKey(String string, Set<String> foundKeys) {
		if (string != null && !string.trim().isEmpty()) {
			if (foundKeys == null) {
				foundKeys = new HashSet<>();
			}
			foundKeys.add(string);
		}

		return foundKeys;
	}

}
