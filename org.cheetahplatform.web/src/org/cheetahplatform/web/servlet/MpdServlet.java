package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.analysis.mpd.IModelingPhaseDetectionStrategy;
import org.cheetahplatform.analysis.mpd.ModelingPhase;
import org.cheetahplatform.analysis.mpd.ModelingPhaseChunkExtractor;
import org.cheetahplatform.analysis.mpd.ModelingPhaseDiagramLineFragmentExtrator;
import org.cheetahplatform.analysis.mpd.ModelingPhaseModel;
import org.cheetahplatform.analysis.ppm.Chunk;
import org.cheetahplatform.common.logging.ProcessInstance;
import org.cheetahplatform.common.logging.db.DatabasePromReader;
import org.cheetahplatform.modeler.DefaultConfiguration;
import org.cheetahplatform.modeler.ModelerConstants;
import org.cheetahplatform.modeler.bpmn.BpmnNotation;
import org.cheetahplatform.modeler.model.Graph;
import org.cheetahplatform.modeler.replay.CommandReplayer;
import org.eclipse.gef.commands.CommandStack;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MpdServlet extends HttpServlet {

	private static final long serialVersionUID = 8715076389168401787L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String processInstance = request.getParameter("processInstance");
		Connection connection = null;

		try {
			connection = ProcessInstanceServlet.getDatabaseConnection();
			ProcessInstance instance = DatabasePromReader.readProcessInstance(Long.parseLong(processInstance), connection);

			IModelingPhaseDetectionStrategy strategy = ModelingPhaseModel.DEFAULT_DETECTION_STRATEGY;
			ModelingPhaseChunkExtractor extractor = new ModelingPhaseChunkExtractor(strategy,
					ModelerConstants.DEFAULT_COMPREHENSION_THRESHOLD, ModelerConstants.DEFAULT_COMPREHENSION_AGGREGATION_THRESHOLD);
			List<Chunk> chunks = extractor.extractChunks(instance);

			BpmnNotation notation = new BpmnNotation();
			DefaultConfiguration configuration = new DefaultConfiguration();
			notation.setDefaultConfigurationValues(configuration);
			Graph graph = new Graph(notation.getAllDescriptors(configuration));
			CommandReplayer replayer = new CommandReplayer(new CommandStack(), graph, instance, configuration);
			List<ModelingPhase> lines = new ModelingPhaseDiagramLineFragmentExtrator().extractLineFragments(graph, replayer, instance,
					chunks);

			response.setContentType("application/json");
			JsonFactory factory = new JsonFactory();
			JsonGenerator generator = factory.createGenerator(response.getOutputStream());
			ObjectMapper mapper = new ObjectMapper(factory);
			mapper.writer().writeValue(generator, lines);
			response.getOutputStream().flush();
		} catch (Exception e) {
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
