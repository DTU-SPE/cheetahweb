package org.cheetahplatform.web.eyetracking.cleaning;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cheetahplatform.web.dto.FilterRequest;

import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilter;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignFisher;

/**
 * Butterworth filter cleaning; with correction for phase shift (formula see resources/butterworth.pdf).
 *
 * @author Stefan
 *
 */
public class ButterworthFilter extends AbstractPupillometryFilter {

	private static final String BUTTERWORTH_HERTZ = "butterworth_hertz";
	private static final String BUTTERWORTH_SAMPLE_RATE = "butterworth_sample_rate";

	public ButterworthFilter(long id) {
		super(id, "Butterworth");
	}

	private void applyFilter(PupillometryFile file, IirFilter filter, PupillometryFileColumn pupilColumn,
			PupillometryFileColumn timestampColumn, double phaseShiftInMs) throws IOException {
		double[] values = PupillometryFileUtils.getPupilValues(file, pupilColumn, true);
		LinkedList<PupillometryFileLine> content = file.getContent();
		Iterator<PupillometryFileLine> iterator = content.iterator();
		int index = 0;

		// shift the values by the phase response
		long startTimestamp = content.get(0).getLong(timestampColumn);
		long currentTimestamp = content.get(0).getLong(timestampColumn);
		while (iterator.hasNext()) {
			PupillometryFileLine current = iterator.next();
			currentTimestamp = current.getLong(timestampColumn);
			current.deleteValue(pupilColumn);
			filter.step(values[index]);
			index++;

			if (currentTimestamp - startTimestamp > -phaseShiftInMs * 1000) {
				break;
			}
		}

		// filter the remaining values
		iterator = content.iterator();
		while (iterator.hasNext() && index < values.length) {
			double filtered = filter.step(values[index]);
			PupillometryFileLine element = iterator.next();
			element.setValue(pupilColumn, filtered);
			index++;
		}

		// fill remaining values with empty values
		while (iterator.hasNext()) {
			iterator.next().deleteValue(pupilColumn);
		}
	}

	@Override
	protected List<PupillometryParameter> getParameters() {
		List<PupillometryParameter> parameters = super.getParameters();
		parameters.add(new PupillometryParameter(BUTTERWORTH_HERTZ, "Hertz (Butterworth Filter)", "4", false));
		parameters.add(new PupillometryParameter(BUTTERWORTH_SAMPLE_RATE, "Sample Rate", "300", false));

		return parameters;
	}

	@Override
	public String run(FilterRequest request, PupillometryFile file, long fileId) throws Exception {
		String hertzRaw = request.getParameter(BUTTERWORTH_HERTZ);
		double hertz = Double.parseDouble(hertzRaw);
		String sampleRateRaw = request.getParameter(BUTTERWORTH_SAMPLE_RATE);
		double sampleRate = Double.parseDouble(sampleRateRaw);

		IirFilterCoefficients butterworth = IirFilterDesignFisher.design(FilterPassType.lowpass, FilterCharacteristicsType.butterworth, 3,
				0, hertz / sampleRate, 0);
		IirFilter filter = new IirFilter(butterworth);

		double omega = 2 * hertz * Math.PI;
		double tmp1 = 2 * omega - Math.pow(omega, 3);
		double tmp2 = 1 - 2 * Math.pow(omega, 2);
		double phaseShift = -Math.atan(tmp1 / tmp2);

		double relativePhaseShift = phaseShift / (2 * Math.PI);
		double timeShiftInMs = relativePhaseShift * (1 / hertz) * 1000;

		PupillometryFileHeader header = file.getHeader();
		PupillometryFileColumn timestampColumn = header.getColumn(request.getTimestampColumn());
		PupillometryFileColumn leftPupil = header.getColumn(request.getLeftPupilColumn());
		applyFilter(file, filter, leftPupil, timestampColumn, timeShiftInMs);
		PupillometryFileColumn rightPupil = header.getColumn(request.getRightPupilColumn());
		applyFilter(file, filter, rightPupil, timestampColumn, timeShiftInMs);

		return "lowpass, " + hertzRaw + " Hz, " + sampleRateRaw + " Hz Samplerate";
	}

}
