package org.cheetahplatform.web.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.eyetracking.cleaning.IAnalysisContributor;
import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFilter;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryDataFilterRegistry;
import org.cheetahplatform.web.eyetracking.cleaning.ThomasMaran1Contributor;
import org.cheetahplatform.web.eyetracking.cleaning.ThomasMaranPersonaExperimentContributor;

public class FilterRequest {
	private static final String NO_ANALYSIS = "no_analysis";
	private List<Long> files;
	private List<Long> filters;
	private Map<String, String> parameters;
	private String analyzeData;
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

	public boolean isAnalyisDefined() {
		return analyzeData != null && !analyzeData.equals(NO_ANALYSIS);
	}

	public String isAnalyzeData() {
		return analyzeData;
	}

	public IAnalysisContributor resolveAnalysisContributor() {
		if (!isAnalyisDefined()) {
			return null;
		}

		if (analyzeData.equals("thomas_maran")) {
			return new ThomasMaran1Contributor();
		}
		if (analyzeData.equals("thomas_maran_persona_experiment")) {
			return new ThomasMaranPersonaExperimentContributor();
		}

		throw new RuntimeException("Unknown analysis: " + analyzeData);
	}

	public void setAnalyzeData(String analyzeData) {
		this.analyzeData = analyzeData;
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
