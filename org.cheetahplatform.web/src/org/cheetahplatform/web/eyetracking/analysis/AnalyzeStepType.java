package org.cheetahplatform.web.eyetracking.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalyzeStepType {
	public static final AnalyzeStepType MEAN_ABSOLUTE = new AnalyzeStepType("mean_absolute", "Absolute Mean",
			"Computes the absolute mean value of the pupil sizes within the stimulus: mean(value - baseline value)");
	public static final AnalyzeStepType MEAN_RELATIVE = new AnalyzeStepType("mean_relative", "Relative Mean",
			"Computes the relative mean value of the pupil sizes within the stimulus: mean(value / baseline value)");

	public static final List<AnalyzeStepType> ALL;

	static {
		List<AnalyzeStepType> all = new ArrayList<>();
		all.add(MEAN_ABSOLUTE);
		all.add(MEAN_RELATIVE);

		ALL = Collections.unmodifiableList(all);
	}

	private String id;
	private String name;
	private String description;

	public AnalyzeStepType(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
