package org.cheetahplatform.web.eyetracking.cleaning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DataProcessingContext {
	private List<DataProcessingResult> results;

	public DataProcessingContext() {
		results = new ArrayList<>();
	}

	public void addAllDataProcessingResults(Collection<DataProcessingResult> toAdd) {
		results.addAll(toAdd);
	}

	public void addDataProcessingResult(DataProcessingResult result) {
		results.add(result);
	}

	public List<DataProcessingResult> getResults() {
		return Collections.unmodifiableList(results);
	}
}
