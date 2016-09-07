package org.cheetahplatform.web.eyetracking.analysis;

import java.util.List;
import java.util.Map;

/**
 * Configuration for a step that cleans pupillometric data.
 *
 * @author stefan.zugal
 *
 */
public class CleanDataConfiguration {
	/**
	 * The filters to be applied in this step.
	 */
	private List<Long> filters;
	/**
	 * The parameters to be used for the filters.
	 */
	private Map<String, String> parameters;
	/**
	 * The decimal separator to be used for writing files.
	 */
	private String decimalSeparator;
	/**
	 * The filename post fix to be used for writing files.
	 */
	private String fileNamePostFix;

	public String getDecimalSeparator() {
		return decimalSeparator;
	}

	public String getFileNamePostFix() {
		return fileNamePostFix;
	}

	public List<Long> getFilters() {
		return filters;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setDecimalSeparator(String decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
	}

	public void setFileNamePostFix(String fileNamePostFix) {
		this.fileNamePostFix = fileNamePostFix;
	}

	public void setFilters(List<Long> filters) {
		this.filters = filters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
}
