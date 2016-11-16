package org.cheetahplatform.web.dto;

import java.util.List;

import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;

public class PreviewTrialDto extends AbstractTrialPreviewDto {
	public PreviewTrialDto(Trial trial, PupillometryFileColumn studioEventColumn, PupillometryFileColumn studioEventDataColumn) {
		super(trial);
		List<String> computeScenes = trial.computeScenes(studioEventColumn, studioEventDataColumn);
		for (String scene : computeScenes) {
			this.scenes.add(new TrialPreviewSceneDto("scene", scene));
		}
	}
}