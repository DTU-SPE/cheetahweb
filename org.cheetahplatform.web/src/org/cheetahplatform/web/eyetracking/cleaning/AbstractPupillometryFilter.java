package org.cheetahplatform.web.eyetracking.cleaning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.dto.FilterRequest;
import org.cheetahplatform.web.dto.PupillometryFilterDto;

public abstract class AbstractPupillometryFilter implements IPupillometryFilter {
	private long id;

	private String name;

	public AbstractPupillometryFilter(long id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public PupillometryFilterDto getDto() {
		return new PupillometryFilterDto(id, name, getParameters());
	}

	@Override
	public long getId() {
		return id;
	}

	public PupillometryFileColumn getLeftPupilColumn(FilterRequest request, PupillometryFileHeader header) {
		return header.getColumn(request.getLeftPupilColumn());
	}

	@Override
	public String getName() {
		return name;
	}

	protected List<PupillometryParameter> getParameters() {
		List<PupillometryParameter> parameters = new ArrayList<>();
		parameters.add(new PupillometryParameter(CheetahWebConstants.TIMESTAMP, "Timestamp", "EyeTrackerTimestamp", true));
		parameters.add(new PupillometryParameter(CheetahWebConstants.LEFT_PUPIL, "Left Pupil", "PupilLeft", true));
		parameters.add(new PupillometryParameter(CheetahWebConstants.RIGHT_PUPIL, "Right Pupil", "PupilRight", true));
		return parameters;
	}

	public double[] getPupilValues(PupillometryFile file, PupillometryFileColumn column) throws IOException {
		return PupillometryFileUtils.getPupilValues(file, column, false);
	}

	public PupillometryFileColumn getRightPupilColumn(FilterRequest request, PupillometryFileHeader header) {
		return header.getColumn(request.getRightPupilColumn());
	}

	public PupillometryFileColumn getTimestampColumn(FilterRequest request, PupillometryFileHeader header) {
		return header.getColumn(request.getTimestampColumn());
	}
}
