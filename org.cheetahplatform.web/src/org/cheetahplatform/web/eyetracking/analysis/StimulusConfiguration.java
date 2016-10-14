package org.cheetahplatform.web.eyetracking.analysis;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = DefaultStimulusConfiguration.class, name = "default"),
		@Type(value = StimulusTriggeredByPreviousScene.class, name = "triggered_by_scene") })
public class StimulusConfiguration {

}
