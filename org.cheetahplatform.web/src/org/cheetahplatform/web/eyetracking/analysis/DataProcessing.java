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
	private String timestampColumn;
	private String leftPupilColumn;
	private String rightPupilColumn;
	public static final String DATA_PROCESSING_TYPE_CLEAN = "clean";

	public DataProcessing(long id, String name, String comment, String timestampColumn, String leftPupilColumn, String rightPupilColumn) {
		this.timestampColumn = timestampColumn;
		this.leftPupilColumn = leftPupilColumn;
		this.rightPupilColumn = rightPupilColumn;
		this.steps = new ArrayList<>();
		this.id = id;
		this.name = name;
		this.comment = comment;
	}

	public void addStep(DataProcessingStep step) {
		steps.add(step);
	}

	public String getComment() {
		return comment;
	}

	public long getId() {
		return id;
	}

	public String getLeftPupilColumn() {
		return leftPupilColumn;
	}

	public String getName() {
		return name;
	}

	public String getRightPupilColumn() {
		return rightPupilColumn;
	}

	public List<DataProcessingStep> getSteps() {
		return steps;
	}

	public String getTimestampColumn() {
		return timestampColumn;
	}

}
