package org.cheetahplatform.web.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class PupilForeshorteningResultsPlotter {
	private static final String TRIAL_COLUMN = "trial";
	private static final String SOURCE_FILE = "results_robot_artificial_light.csv";
	// private static final String SOURCE_FILE = "results_robot2_daylight.csv";
	private static final String PATH = "C:\\Users\\Jakob\\Desktop\\Pupil Foreshortening Error\\";
	private static String COLUMN_TO_ANALYZE = "mean_absolute_pupil_average";

	public static void main(String[] args) throws FileNotFoundException, IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(PATH + SOURCE_FILE)))) {
			String[] headerTokens = reader.readLine().split(";");

			int columnIndex = -1;
			int trialColumn = -1;
			for (int i = 0; i < headerTokens.length; i++) {
				String header = headerTokens[i];
				if (COLUMN_TO_ANALYZE.equals(header)) {
					columnIndex = i;
				}

				if (TRIAL_COLUMN.equals(header)) {
					trialColumn = i;
				}
			}

			if (columnIndex < 0) {
				throw new IllegalThreadStateException("Could not find column");
			}

			if (trialColumn < 0) {
				throw new IllegalStateException("Could not find trial column");
			}

			double minimum = Double.POSITIVE_INFINITY;
			double maximum = Double.NEGATIVE_INFINITY;
			Map<Integer, Double> trialToValue = new HashMap<>();

			String line = reader.readLine();
			while (line != null) {
				String[] values = line.split(";");
				String tmpValue = values[columnIndex];
				String tmpTrial = values[trialColumn];

				int trial = Integer.valueOf(tmpTrial);
				double value = Double.valueOf(tmpValue);
				trialToValue.put(trial, value);

				if (value < minimum) {
					minimum = value;
				}
				if (value > maximum) {
					maximum = value;
				}

				line = reader.readLine();
			}

			double range = maximum - minimum;
			System.out.println("min " + minimum);
			System.out.println("max " + maximum);

			System.out.println(range);
			Image image = new Image(Display.getDefault(), 1920, 1080);
			DecimalFormat format = new DecimalFormat("0.000");
			GC gc = new GC(image);
			for (int column = 1; column <= 16; column++) {
				for (int row = 1; row <= 12; row++) {
					double value = trialToValue.get((row - 1) * 16 + column);
					double percent = (maximum - value) / range;

					int red = (int) (percent * 255);
					int green = (int) ((1 - percent) * 255);
					Color color = new Color(Display.getDefault(), red, green, 0);
					gc.setForeground(color);

					gc.drawText(format.format(value), column * (1920 / 16), row * (1080 / 12));
				}
			}

			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[] { image.getImageData() };
			loader.save(PATH + "plot_" + COLUMN_TO_ANALYZE + "_" + SOURCE_FILE + ".png", SWT.IMAGE_PNG);

			gc.dispose();
		}
	}
}
