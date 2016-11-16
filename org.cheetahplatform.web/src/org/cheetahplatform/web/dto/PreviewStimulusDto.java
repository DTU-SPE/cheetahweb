package org.cheetahplatform.web.dto;

import java.util.Collections;
import java.util.List;

import org.cheetahplatform.web.eyetracking.analysis.Stimulus;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class PreviewStimulusDto extends AbstractTrialPreviewDto {
	public PreviewStimulusDto(Trial trial, PupillometryFileColumn studioEventColumn, PupillometryFileColumn studioEventDataColumn) {
		super(trial);

		List<String> scenes = trial.computeScenes(studioEventColumn, studioEventDataColumn);
		Stimulus stimulus = trial.getStimulus();

		List<String> stimulusScenes = Collections.emptyList();
		if (stimulus != null) {
			stimulusScenes = stimulus.computeScenes(studioEventColumn, studioEventDataColumn);
		}

		for (String scene : scenes) {
			boolean isStimulusScene = stimulusScenes.contains(scene);
			if (isStimulusScene) {
				if (stimulusScenes.indexOf(scene) == 0) {
					this.scenes.add(new TrialPreviewSceneDto("marker", "Start of stimulus"));
				}
				this.scenes.add(new TrialPreviewSceneDto("stimulus", scene));
				if (stimulusScenes.indexOf(scene) == stimulusScenes.size() - 1) {
					this.scenes.add(new TrialPreviewSceneDto("marker", "End of stimulus"));
				}
			} else {
				this.scenes.add(new TrialPreviewSceneDto("scene", scene));
			}
		}
	}
}