package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.analysis.mpd.IModelingPhaseDetectionStrategy;
import org.cheetahplatform.analysis.mpd.ModelingPhaseChunkExtractor;
import org.cheetahplatform.analysis.mpd.ModelingPhaseModel;
import org.cheetahplatform.analysis.mpd.ProcessOfProcessModelingIterationsExtractor;
import org.cheetahplatform.analysis.ppm.Chunk;
import org.cheetahplatform.analysis.ppm.IPpmStatistic;
import org.cheetahplatform.analysis.ppm.ProcessOfProcessModelingIteration;
import org.cheetahplatform.analysis.ppm.statistic.AverageIterationChunkSizeStatistic;
import org.cheetahplatform.analysis.ppm.statistic.DeleteIterationsStatistic;
import org.cheetahplatform.analysis.ppm.statistic.MaxModelingChunkSizeStatistic;
import org.cheetahplatform.analysis.ppm.statistic.NumberOfIterationsStatistic;
import org.cheetahplatform.analysis.ppm.statistic.ReconciliationBreakStatistic;
import org.cheetahplatform.analysis.ppm.statistic.ShareOfComprehensionStatistic;
import org.cheetahplatform.analysis.ppm.statistic.ThreePhaseIterationsStatistic;
import org.cheetahplatform.analysis.ppm.statistic.TotalComprehensionDurationStatistic;
import org.cheetahplatform.analysis.ppm.statistic.TotalModelingDurationStatistic;
import org.cheetahplatform.analysis.ppm.statistic.TotalReconciliationDurationStatistic;
import org.cheetahplatform.analysis.ppm.statistic.layout.AverageTimeAfterLayoutStatistic;
import org.cheetahplatform.analysis.ppm.statistic.layout.DurationBetweenLayoutAndUndoStatistic;
import org.cheetahplatform.analysis.ppm.statistic.layout.LayoutContinuumDurationStatistic;
import org.cheetahplatform.analysis.ppm.statistic.layout.LayoutContinuumModelingStepsStatistic;
import org.cheetahplatform.analysis.ppm.statistic.layout.LayoutContinuumPpmStatistic;
import org.cheetahplatform.analysis.ppm.statistic.layout.LayoutDurationStatistic;
import org.cheetahplatform.analysis.ppm.statistic.layout.SuccessfulLayoutCounterStatistic;
import org.cheetahplatform.analysis.ppm.statistic.layout.TimesAfterLayoutStatistic;
import org.cheetahplatform.analysis.ppm.statistic.layout.UndoLayoutStatistic;
import org.cheetahplatform.analysis.ppm.statistic.layout.UnsuccesfulLayoutStatistic;
import org.cheetahplatform.analysis.replay.ReplayModel;
import org.cheetahplatform.common.Activator;
import org.cheetahplatform.common.CommonConstants;
import org.cheetahplatform.common.logging.ProcessInstance;
import org.cheetahplatform.common.logging.db.DatabasePromReader;
import org.cheetahplatform.common.logging.db.IDatabaseConnector;
import org.cheetahplatform.common.ui.dialog.ProcessInstanceDatabaseHandle;
import org.cheetahplatform.modeler.ModelerConstants;
import org.cheetahplatform.modeler.replay.CommandReplayer;
import org.cheetahplatform.web.dto.MetricsDto;

public class MetricServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 6764964329724397272L;

	private List<IPpmStatistic> createStatistics() {
		List<IPpmStatistic> statistics = new ArrayList<IPpmStatistic>();
		statistics.add(new AverageIterationChunkSizeStatistic());
		statistics.add(new SuccessfulLayoutCounterStatistic());
		statistics.add(new UnsuccesfulLayoutStatistic());
		statistics.add(new ThreePhaseIterationsStatistic());
		statistics.add(new ReconciliationBreakStatistic());
		statistics.add(new NumberOfIterationsStatistic());
		statistics.add(new ShareOfComprehensionStatistic());
		statistics.add(new DeleteIterationsStatistic());
		statistics.add(new TotalComprehensionDurationStatistic());
		statistics.add(new TotalModelingDurationStatistic());
		statistics.add(new TotalReconciliationDurationStatistic());
		statistics.add(new UndoLayoutStatistic());
		statistics.add(new DurationBetweenLayoutAndUndoStatistic());
		statistics.add(new LayoutDurationStatistic());
		statistics.add(new TimesAfterLayoutStatistic());
		statistics.add(new AverageTimeAfterLayoutStatistic());
		statistics.add(new LayoutContinuumDurationStatistic());
		statistics.add(new LayoutContinuumModelingStepsStatistic());
		statistics.add(new LayoutContinuumPpmStatistic());
		statistics.add(new MaxModelingChunkSizeStatistic());

		return statistics;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection connection = null;
		int instanceId = Integer.parseInt(request.getParameter("processInstance"));

		try {
			IDatabaseConnector connector = Activator.getDatabaseConnector();
			connector.setAdminCredentials("cheetah", "cheetah");
			connector.setDatabaseURL("jdbc:mysql://localhost/2015_01_sandra_obernhummer");

			connection = ProcessInstanceServlet.getDatabaseConnection();
			ProcessInstance instance = DatabasePromReader.readProcessInstance(instanceId, connection);
			CommandReplayer replayer = createReplayer(instance);
			ProcessInstanceDatabaseHandle handle = new ProcessInstanceDatabaseHandle(instanceId, instance.getId(),
					instance.getAttributes(), "dummy");
			handle.setInstance(instance);
			ReplayModel replayModel = new ReplayModel(replayer.getStack(), handle, replayer.getGraph(), replayer);

			List<IPpmStatistic> statistics = createStatistics();
			String modelId = String.valueOf(instanceId);
			String workflowId = replayModel.getProcessInstance().getAttribute(CommonConstants.ATTRIBUTE_EXPERIMENT_PROCESS_INSTANCE);

			String experiment = "";
			if (replayModel.getProcessInstance().isAttributeDefined(CommonConstants.ATTRIBUTE_PROCESS)) {
				experiment = replayModel.getProcessInstance().getAttribute(CommonConstants.ATTRIBUTE_PROCESS);
			}
			MetricsDto metricsDto = new MetricsDto(modelId, workflowId, experiment);
			IModelingPhaseDetectionStrategy strategy = ModelingPhaseModel.DEFAULT_DETECTION_STRATEGY;
			ModelingPhaseChunkExtractor extractor = new ModelingPhaseChunkExtractor(strategy,
					ModelerConstants.DEFAULT_COMPREHENSION_THRESHOLD, ModelerConstants.DEFAULT_COMPREHENSION_AGGREGATION_THRESHOLD);
			List<Chunk> chunks = extractor.extractChunks(instance);

			ProcessOfProcessModelingIterationsExtractor iterationsExtractor = new ProcessOfProcessModelingIterationsExtractor();
			List<ProcessOfProcessModelingIteration> iterations = iterationsExtractor.extractIterations(chunks);

			for (IPpmStatistic statistic : statistics) {
				String value = statistic.getValue(replayModel.getProcessInstanceDatabaseHandle(), chunks, iterations);
				metricsDto.addMetric(statistic.getName(), value);
			}

			AbstractCheetahServlet.writeJson(response, metricsDto);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// ignore
				}
			}
		}
	}
}
