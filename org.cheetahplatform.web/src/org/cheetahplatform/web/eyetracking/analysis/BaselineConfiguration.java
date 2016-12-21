package org.cheetahplatform.web.eyetracking.analysis;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "baselineCalculation")
@JsonSubTypes({ @Type(value = BaselineTriggeredBySceneConfiguration.class, name = "baseline-scene-trigger"),
		@Type(value = DurationBeforeStimulusBaselineConfiguration.class, name = "baseline-duration-before-stimulus") })
public class BaselineConfiguration {
	private boolean noBaseline;

	public boolean isNoBaseline() {
		return noBaseline;
	}

	public void setNoBaseline(boolean noBaseline) {
		this.noBaseline = noBaseline;
	}
}
