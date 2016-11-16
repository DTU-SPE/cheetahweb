package org.cheetahplatform.web.eyetracking.analysis;

import org.eclipse.core.runtime.Assert;

public abstract class AbstractTrialAnalyzer implements ITrialAnalyzer {
	protected static final String RESULT_SEPARATOR = "_";
	protected static final String PUPIL_LEFT = "pupil_left";
	protected static final String PUPIL_RIGHT = "pupil_right";
	protected static final String PUPIL_AVERAGE = "pupil_average";

	protected AnalyzeConfiguration config;
	private String name;
	protected DataProcessing dataProcessing;

	public AbstractTrialAnalyzer(AnalyzeConfiguration config, DataProcessing processing, String name) {
		Assert.isNotNull(config);
		Assert.isNotNull(name);
		Assert.isLegal(!name.trim().isEmpty());
		Assert.isNotNull(processing);
		this.config = config;
		this.dataProcessing = processing;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}