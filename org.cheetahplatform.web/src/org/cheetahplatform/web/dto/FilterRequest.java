package org.cheetahplatform.web.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFilter;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryDataFilterRegistry;

public class FilterRequest {
	private List<Long> files;
	private List<Long> filters;
	private Map<String, String> parameters;
	private String decimalSeparator;
	private String fileNamePostFix;

	public FilterRequest() {
		// JSON
	}

	public String getDecimalSeparator() {
		return decimalSeparator;
	}

	public String getFileNamePostFix() {
		return fileNamePostFix;
	}

	public List<Long> getFiles() {
		return files;
	}

	public List<Long> getFilterIds() {
		return filters;
	}

	public String getLeftPupilColumn() {
		return parameters.get(CheetahWebConstants.LEFT_PUPIL);
	}

	public String getParameter(String name) {
		return parameters.get(name);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public List<IPupillometryFilter> getPupillometryFilters() {
		List<IPupillometryFilter> list = new ArrayList<IPupillometryFilter>();

		for (Long id : filters) {
			list.add(PupillometryDataFilterRegistry.getFilter(id));
		}

		return list;
	}

	public String getRightPupilColumn() {
		return parameters.get(CheetahWebConstants.RIGHT_PUPIL);
	}

	public String getTimestampColumn() {
		return parameters.get(CheetahWebConstants.TIMESTAMP);
	}

	public void setDecimalSeparator(String decimalSeparator) {
		this.decimalSeparator = decimalSeparator;
	}

	public void setFileNamePostFix(String fileNamePostFix) {
		this.fileNamePostFix = fileNamePostFix;
	}

	public void setFiles(List<Long> files) {
		this.files = files;
	}

	public void setFilters(List<Long> filters) {
		this.filters = filters;
	}

	public void setParameters(Map<String, String> columns) {
		this.parameters = columns;
	}
}
