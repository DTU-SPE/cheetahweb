package org.cheetahplatform.web.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cheetahplatform.web.eyetracking.analysis.BaselineConfiguration;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.DefaultStimulusConfiguration;
import org.cheetahplatform.web.eyetracking.analysis.Stimulus;
import org.cheetahplatform.web.eyetracking.analysis.Trial;
import org.cheetahplatform.web.eyetracking.analysis.TrialConfiguration;
import org.cheetahplatform.web.eyetracking.analysis.TrialEvaluation;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class PupilForeshorteningErrorFixationExtractor {
	private static final String PLOT_FIXATIONS = "plot_fixations";

	private static class Fixation {
		private Point point;
		private int count;

		public Fixation(Point point) {
			super();
			this.point = point;
			count = 1;
		}

		public Point getPoint() {
			return point;
		}

		public void increaseCount() {
			count++;
		}

		@Override
		public String toString() {
			return point.toString() + " occurred " + count + " times";
		}
	}

	private static final String MAKER_STIMULUS = "Stimulus";

	private static final String MARKER_TRIAL_START = "TrialStart";
	private static final String COLUMN_FIXATION_POINT_Y = "FixationPointY (MCSpx)";

	private static final String COLUMN_FIXATION_POINT_X = "FixationPointX (MCSpx)";

	private static final int POINT_SIZE = 6;

	private static final String SOURCE_FILE = "Forshortening_artificial_light_selected_12122016_relabeled_scenes.tsv";
	private static final String PATH = "C:\\Users\\Jakob\\Desktop\\PupilForeShorteningError\\";

	public static void main(String[] args) throws Exception {
		DataProcessing dataProcessing = new DataProcessing(-1, "foreshortening", "", "EyeTrackerTimestamp", "PupilLeft", "PupilRight", ",",
				"");

		TrialConfiguration trialConfiguration = new TrialConfiguration();
		trialConfiguration.setTrialStart(MARKER_TRIAL_START);
		trialConfiguration.setUseTrialStartForTrialEnd(true);
		trialConfiguration.setIgnoredTrials(0);
		DefaultStimulusConfiguration defaultStimulusConfiguration = new DefaultStimulusConfiguration();
		defaultStimulusConfiguration.setStimulusStart(MAKER_STIMULUS);
		defaultStimulusConfiguration.setStimulusEnd(MAKER_STIMULUS);
		trialConfiguration.setStimulus(defaultStimulusConfiguration);
		BaselineConfiguration baseline = new BaselineConfiguration();
		baseline.setNoBaseline(true);
		trialConfiguration.setBaseline(baseline);

		StandaloneTrialDetector detector = new StandaloneTrialDetector(dataProcessing, trialConfiguration, PATH + SOURCE_FILE);

		TrialEvaluation result = detector.detectTrials(true, false, false);

		PupillometryFile pupillometryFile = detector.getPupillometryFile();
		PupillometryFileColumn fixationXColumn = pupillometryFile.getColumn(COLUMN_FIXATION_POINT_X);
		PupillometryFileColumn fixationYColumn = pupillometryFile.getColumn(COLUMN_FIXATION_POINT_Y);

		Map<Integer, Collection<Fixation>> trialToFixations = new HashMap<>();

		List<Trial> trials = result.getTrials();
		System.out.println(trials.size());
		for (Trial trial : trials) {
			Map<Point, Fixation> fixations = new java.util.HashMap<>();

			Stimulus stimulus = trial.getStimulus();
			for (PupillometryFileLine pupillometryFileLine : stimulus.getLines()) {
				int x = 0;
				int y = 0;

				if (!pupillometryFileLine.isEmpty(fixationXColumn)) {
					x = pupillometryFileLine.getInteger(fixationXColumn);
				}

				if (!pupillometryFileLine.isEmpty(fixationYColumn)) {
					y = pupillometryFileLine.getInteger(fixationYColumn);
				}

				if (x > 0 && y > 0) {
					Point point = new Point(x, y);

					if (!fixations.containsKey(point)) {
						Fixation fixation = new Fixation(point);
						fixations.put(point, fixation);
					} else {
						fixations.get(point).increaseCount();
					}
				}
			}

			trialToFixations.put(trial.getTrialNumber(), fixations.values());

		}

		Image image = new Image(Display.getDefault(), 1920, 1080);
		GC gc = new GC(image);
		for (int row = 1; row <= 12; row++) {
			for (int column = 1; column <= 16; column++) {
				int trialnumer = (row - 1) * 16 + column;
				System.out.println("Trial " + trialnumer);

				// shift to the half
				int x = (column * (1920 / 16)) - 60;
				int y = (row * (1080 / 12)) - 45;
				System.out.println("Expected: " + new Point(x, y));

				gc.setForeground(new Color(Display.getCurrent(), 255, 0, 0));
				Collection<Fixation> fixations = trialToFixations.get(trialnumer);
				for (Fixation fixation : fixations) {
					Point point = fixation.getPoint();
					gc.drawLine(point.x, point.y, x, y);
					gc.drawOval(point.x - POINT_SIZE / 2, point.y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);
					System.out.println(fixation);
				}

				gc.setForeground(new Color(Display.getCurrent(), 0, 218, 56));
				gc.drawOval(x - POINT_SIZE / 2, y - POINT_SIZE / 2, POINT_SIZE, POINT_SIZE);

				System.out.println("---");
			}
		}

		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { image.getImageData() };
		loader.save(PATH + PLOT_FIXATIONS + "_" + SOURCE_FILE + ".png", SWT.IMAGE_PNG);

		gc.dispose();
	}
}
