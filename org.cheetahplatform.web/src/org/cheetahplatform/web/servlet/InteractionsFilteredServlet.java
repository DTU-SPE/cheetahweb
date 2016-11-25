package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.common.logging.Attribute;
import org.cheetahplatform.common.logging.PromLogger;
import org.cheetahplatform.common.logging.db.DatabaseUtil;
import org.cheetahplatform.modeler.command.AbstractGraphCommand;
import org.cheetahplatform.web.dto.InteractionDto;

public class InteractionsFilteredServlet extends AbstractCheetahServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 7841411011135711079L;

	private static String getStringOutOfDatasetAttributes(List<Attribute> attributes, String attributeName) {
		for (Attribute attribute : attributes) {
			String name = attribute.getName();
			if (name.equals(attributeName)) {
				return attribute.getContent();
			}
		}
		return null;
	}

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		long ppmInstanceId = Long.parseLong(request.getParameter("ppmInstanceId"));
		String selectedInteractions = request.getParameter("interactions");

		selectedInteractions = selectedInteractions.replace(",", "%\" or data like \"%,descriptorBPMN.");

		String sql = "select database_id, timestamp, type, workflow_element,data from audittrail_entry where process_instance="
				+ ppmInstanceId + " and (data like \"%,descriptorBPMN." + selectedInteractions + "%\")";
		ResultSet result = connection.createStatement().executeQuery(sql);

		List<InteractionDto> interactions = new ArrayList<InteractionDto>();
		while (result.next()) {
			int id = result.getInt("database_id");
			// convert to nanoseconds
			// in case adjustments are necessary for specific process instances: update audittrail_entry set
			// timestamp=cast((cast(audittrail_entry.timestamp as UNSIGNED INTEGER) + MILLISECONDS_TO_SHIFT) as CHAR(255)) where
			// process_instance=ID;
			long timestamp = (result.getLong("timestamp") * 1000);
			String workflowElement = result.getString("workflow_element");
			String type = result.getString("type");
			String data = result.getString("data");

			List<Attribute> fromDataBaseRepresentation = DatabaseUtil.fromDataBaseRepresentation(data);
			String startTimeAsString = null;

			if (type.equals("RENAME")) {
				startTimeAsString = getStringOutOfDatasetAttributes(fromDataBaseRepresentation, "rename_start_time");
			}
			if (type.equals("CREATE_NODE")) {
				startTimeAsString = getStringOutOfDatasetAttributes(fromDataBaseRepresentation, "add_node_start_time");
			}

			Long startTime = null;
			if (startTimeAsString != null) {
				startTime = (Long.parseLong(startTimeAsString) * 1000);
			}

			if (PromLogger.GROUP_EVENT_END.equals(type) || PromLogger.GROUP_EVENT_START.equals(type)) {
				continue;
			}

			if (AbstractGraphCommand.VSCROLL.equals(type) || AbstractGraphCommand.HSCROLL.equals(type)) {
				continue;
			}

			String typeToDisplay = null;
			if (type.equals("CREATE_NODE") | type.equals("CREATE_EDGE")) {
				typeToDisplay = "Create";
			} else if (type.equals("DELETE_NODE") | type.equals("DELETE_EDGE")) {
				typeToDisplay = "Delete";
			} else if (type.equals("MOVE_NODE")) {
				typeToDisplay = "Move";
			} else if (type.equals("CREATE_EDGE_BENDPOINT")) {
				typeToDisplay = "Create bendpoint";
			} else if (type.equals("CREATE_EDGE_BENDPOINT")) {
				typeToDisplay = "Create bendpoint";
			} else if (type.equals("MOVE_EDGE_BENDPOINT")) {
				typeToDisplay = "Move bendpoint";
			} else if (type.equals("RENAME")) {
				typeToDisplay = "Rename";
			} else if (type.equals("MOVE_EDGE_LABEL")) {
				typeToDisplay = "Move";
			} else if (type.equals("DELETE_EDGE_BENDPOINT")) {
				typeToDisplay = "Delete bendpoint";
			} else if (type.equals("RECONNECT_EDGE")) {
				typeToDisplay = "Reconnect";
			}

			interactions.add(new InteractionDto(id, timestamp, workflowElement, type, typeToDisplay, startTime));
		}

		writeJson(response, interactions);
	}
}
