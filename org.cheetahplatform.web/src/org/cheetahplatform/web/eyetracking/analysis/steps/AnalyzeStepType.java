package org.cheetahplatform.web.eyetracking.analysis.steps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalyzeStepType {
	public static final AnalyzeStepType MEAN_ABSOLUTE = new AnalyzeStepType("mean_absolute", "Mean: Absolute",
			"Computes the absolute mean value of the pupil sizes within the stimulus: mean(value)");
	public static final AnalyzeStepType MEAN_RELATIVE_DIVIDED = new AnalyzeStepType("mean_relative_divided", "Mean: Relative Divided",
			"Computes the relative mean value of the pupil sizes within the stimulus by dividing it through the baseline: mean(value / mean(baseline value))");
	public static final AnalyzeStepType MEAN_RELATIVE_SUBTRACTED = new AnalyzeStepType("mean_relative_subtracted",
			"Mean: Relative Subtracted",
			"Computes the relative mean value of the pupil sizes within the stimulus by subtracting the baseline: mean(value - mean(baseline value))");

	public static final AnalyzeStepType STANDARD_DEVIATION_ABSOLUTE = new AnalyzeStepType("standard_deviation_deviation_absolute",
			"Standard Deviation: Absolute",
			"Computes the standard deviation of the pupil sizes within the stimulus: standard_deviation(value)");
	public static final AnalyzeStepType STANDARD_DEVIATION_RELATIVE_DIVIDED = new AnalyzeStepType("standard_deviation_relative_divided",
			"Standard Deviation: Relative Divided",
			"Computes the relative standard deviation of the pupil sizes within the stimulus by dividing it through the baseline: standard_deviation(value / mean(baseline value))");
	public static final AnalyzeStepType STANDARD_DEVIATION_RELATIVE_SUBTRACTED = new AnalyzeStepType(
			"standard_deviation_relative_subtracted", "Standard Deviation: Relative Subtracted",
			"Computes the relative standard deviation of the pupil sizes within the stimulus by subtracting the baseline: standard_deviation(value - mean(baseline value))");

	public static final AnalyzeStepType STANDARD_ERROR_ABSOLUTE = new AnalyzeStepType("standard_error_absolute", "Standard Error: Absolute",
			"Computes the standard error of the pupil sizes within the stimulus: standard_error(value)");
	public static final AnalyzeStepType STANDARD_ERROR_RELATIVE_DIVIDED = new AnalyzeStepType("standard_error_relative_divided",
			"Standard Error: Relative Divided",
			"Computes the relative standard error of the pupil sizes within the stimulus by dividing it through the baseline: standard_error(value / mean(baseline value))");
	public static final AnalyzeStepType STANDARD_ERROR_RELATIVE_SUBTRACTED = new AnalyzeStepType("standard_error_relative_subtracted",
			"Standard Error: Relative Subtracted",
			"Computes the relative standard error of the pupil sizes within the stimulus by subtracting the baseline: standard_error(value - mean(baseline value))");

	public static final AnalyzeStepType MEDIAN_ABSOLUTE = new AnalyzeStepType("median_absolute", "Median: Absolute",
			"Computes the median of the pupil sizes within the stimulus: median(value)");
	public static final AnalyzeStepType MEDIAN_RELATIVE_DIVIDED = new AnalyzeStepType("median_relative_divided", "Median: Relative Divided",
			"Computes the relative median of the pupil sizes within the stimulus by dividing it through the baseline: median(value / mean(baseline value))");
	public static final AnalyzeStepType MEDIAN_RELATIVE_SUBTRACTED = new AnalyzeStepType("median_relative_subtracted",
			"Median: Relative Subtracted",
			"Computes the relative median of the pupil sizes within the stimulus by subtracting the baseline: median(value - mean(baseline value))");

	public static final AnalyzeStepType MAXIMUM_ABSOLUTE = new AnalyzeStepType("maximum_absolute", "Maximum: Absolute",
			"Computes the maximum of the pupil sizes within the stimulus: maximum(value)");
	public static final AnalyzeStepType MAXIMUM_RELATIVE_DIVIDED = new AnalyzeStepType("maximum_relative_divided",
			"Maximum: Relative Divided",
			"Computes the relative maximum of the pupil sizes within the stimulus by dividing it through the baseline: maximum(value / mean(baseline value))");
	public static final AnalyzeStepType MAXIMUM_RELATIVE_SUBTRACTED = new AnalyzeStepType("maximum_relative_subtracted",
			"Maximum: Relative Subtracted",
			"Computes the relative maximum of the pupil sizes within the stimulus by subtracting the baseline: maximum(value - mean(baseline value))");

	public static final AnalyzeStepType MINIMUM_ABSOLUTE = new AnalyzeStepType("minimum_absolute", "Minimum: Absolute",
			"Computes the minimum of the pupil sizes within the stimulus: minimum(value)");
	public static final AnalyzeStepType MINIMUM_RELATIVE_DIVIDED = new AnalyzeStepType("minimum_relative_divided",
			"Minimum: Relative Divided",
			"Computes the relative minimum of the pupil sizes within the stimulus by dividing it through the baseline: minimum(value / mean(baseline value))");
	public static final AnalyzeStepType MINIMUM_RELATIVE_SUBTRACTED = new AnalyzeStepType("minimum_relative_subtracted",
			"Minimum: Relative Subtracted",
			"Computes the relative minimum of the pupil sizes within the stimulus by subtracting the baseline: minimum(value - mean(baseline value))");

	public static final List<AnalyzeStepType> ALL;

	static {
		List<AnalyzeStepType> all = new ArrayList<>();
		all.add(MEAN_ABSOLUTE);
		all.add(MEAN_RELATIVE_DIVIDED);
		all.add(MEAN_RELATIVE_SUBTRACTED);
		all.add(STANDARD_DEVIATION_ABSOLUTE);
		all.add(STANDARD_DEVIATION_RELATIVE_DIVIDED);
		all.add(STANDARD_DEVIATION_RELATIVE_SUBTRACTED);
		all.add(STANDARD_ERROR_ABSOLUTE);
		all.add(STANDARD_ERROR_RELATIVE_DIVIDED);
		all.add(STANDARD_ERROR_RELATIVE_SUBTRACTED);
		all.add(MEDIAN_ABSOLUTE);
		all.add(MEDIAN_RELATIVE_DIVIDED);
		all.add(MEDIAN_RELATIVE_SUBTRACTED);
		all.add(MAXIMUM_ABSOLUTE);
		all.add(MAXIMUM_RELATIVE_DIVIDED);
		all.add(MAXIMUM_RELATIVE_SUBTRACTED);
		all.add(MINIMUM_ABSOLUTE);
		all.add(MINIMUM_RELATIVE_DIVIDED);
		all.add(MINIMUM_RELATIVE_SUBTRACTED);

		ALL = Collections.unmodifiableList(all);
	}

	public static AnalyzeStepType byId(String id) {
		for (AnalyzeStepType type : ALL) {
			if (type.getId().equals(id)) {
				return type;
			}
		}

		throw new IllegalArgumentException("Unknown analyze step type: " + id);
	}

	private String id;
	private String name;

	private String description;

	private AnalyzeStepType(String id, String name, String description) {
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
