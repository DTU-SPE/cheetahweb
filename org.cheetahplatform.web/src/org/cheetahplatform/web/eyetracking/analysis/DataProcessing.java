package org.cheetahplatform.web.eyetracking.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines data processing for a study, e.g., cleaning or analyzing data.
 *
 * @author stefan.zugal
 *
 */
public class DataProcessing {
	private long id;
	private String name;
	private String comment;
	private List<DataProcessingStep> steps;

	public DataProcessing(long id, String name, String comment) {
		this.steps = new ArrayList<>();
		this.id = id;
		this.name = name;
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<DataProcessingStep> getSteps() {
		return steps;
	}

}
