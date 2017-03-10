package org.cheetahplatform.web.eyetracking.cleaning;

import java.io.IOException;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.cheetahplatform.web.dto.FilterRequest;

public class StandardDeviatonFilter extends AbstractPupillometryFilter {

	public StandardDeviatonFilter(long id) {
		super(id, "Standard Deviation");
	}

	public String cleanPupil(PupillometryFile file, PupillometryFileColumn column) throws IOException {
		double[] pupils = getPupilValues(file, column);
		double standardDeviation = new StandardDeviation().evaluate(pupils);
		double mean = new Mean().evaluate(pupils);
		double upperBound = mean + 3 * standardDeviation;
		double lowerBound = mean - 3 * standardDeviation;
		int numberRemovedValues = 0;
		List<PupillometryFileLine> content = file.getContent();
		for (PupillometryFileLine line : content) {
			if (line.isEmpty(column)) {
				continue;
			}
			double value = line.getDouble(column);
			if (value < lowerBound || value > upperBound) {
				line.setValue(column, 0);
				numberRemovedValues++;
			}
		}

		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append(column.getName());
		resultBuilder.append(": ");
		resultBuilder.append("M=");
		resultBuilder.append(mean);
		resultBuilder.append(", ");
		resultBuilder.append("SD=");
		resultBuilder.append(standardDeviation);
		resultBuilder.append(", ");
		resultBuilder.append("Lo=");
		resultBuilder.append(lowerBound);
		resultBuilder.append(", ");
		resultBuilder.append("Hi=");
		resultBuilder.append(upperBound);
		resultBuilder.append(", ");
		resultBuilder.append("Removed=");
		resultBuilder.append(numberRemovedValues);
		resultBuilder.append(";");

		return resultBuilder.toString();
	}

	@Override
	public String run(FilterRequest request, PupillometryFile file, long fileId) throws Exception {
		PupillometryFileHeader header = file.getHeader();
		PupillometryFileColumn leftPupilColumn = getLeftPupilColumn(request, header);
		PupillometryFileColumn rightPupilColumn = getRightPupilColumn(request, header);

		String result = "";
		result += cleanPupil(file, leftPupilColumn);
		result += cleanPupil(file, rightPupilColumn);
		return result;
	}
}
