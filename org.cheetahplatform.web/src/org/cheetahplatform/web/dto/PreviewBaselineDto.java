package org.cheetahplatform.web.dto;

import java.util.Collections;
import java.util.List;

import org.cheetahplatform.web.eyetracking.analysis.Stimulus;
import org.cheetahplatform.web.eyetracking.analysis.TimeBeforeEventPupillometryFileSectionIdentifier;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class PreviewBaselineDto extends AbstractTrialPreviewDto {
	public PreviewBaselineDto(Trial trial, PupillometryFileColumn studioEventColumn, PupillometryFileColumn studioEventDataColumn) {
		super(trial);

		List<String> scenes = trial.computeScenes(studioEventColumn, studioEventDataColumn);
		Stimulus stimulus = trial.getStimulus();
		List<String> stimulusScenes = Collections.emptyList();
		if (stimulus != null) {
			stimulusScenes = stimulus.computeScenes(studioEventColumn, studioEventDataColumn);
		}
		List<String> baseLineScenes = Collections.emptyList();
		if (trial.getBaseline() != null) {
			baseLineScenes = trial.getBaseline().computeScenes(studioEventColumn, studioEventDataColumn);
		}

		for (String scene : scenes) {
			if (baseLineScenes.contains(scene)) {
				if (baseLineScenes.indexOf(scene) == 0) {
					this.scenes.add(new TrialPreviewSceneDto("marker", "Start of baseline"));
				}
				this.scenes.add(new TrialPreviewSceneDto("stimulus", scene));
				if (baseLineScenes.indexOf(scene) == baseLineScenes.size() - 1) {
					this.scenes.add(new TrialPreviewSceneDto("marker", "End of baseline"));
				}
				continue;
			}

			if (stimulusScenes.contains(scene)) {
				if (stimulusScenes.indexOf(scene) == 0) {
					if (trial.getBaseline() != null && baseLineScenes.isEmpty()) {
						if (TimeBeforeEventPupillometryFileSectionIdentifier.BASELINE_TYPE.equals(trial.getBaseline().getType())) {
							this.scenes.add(new TrialPreviewSceneDto("marker", "Start of baseline"));
							this.scenes.add(new TrialPreviewSceneDto("marker", "End of baseline"));
						}
					}

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