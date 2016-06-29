package org.cheetahplatform.web.synchronize;

import java.util.List;

import org.cheetahplatform.web.dto.DatabaseConfigurationDto;
import org.cheetahplatform.web.dto.StudyDto;

public class SynchronizeStudiesDto {
	private List<StudyDto> studies;
	private DatabaseConfigurationDto configuration;

	public DatabaseConfigurationDto getConfiguration() {
		return configuration;
	}

	public List<StudyDto> getStudies() {
		return studies;
	}

	public void setConfiguration(DatabaseConfigurationDto configuration) {
		this.configuration = configuration;
	}

	public void setStudies(List<StudyDto> studies) {
		this.studies = studies;
	}

}
