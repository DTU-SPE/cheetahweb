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

import org.cheetahplatform.common.logging.PromLogger;
import org.cheetahplatform.modeler.command.AbstractGraphCommand;
import org.cheetahplatform.web.dto.InteractionDto;

public class InteractionsServlet extends AbstractCheetahServlet {

	private static final long serialVersionUID = 2034166510637921489L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		long ppmInstanceId = Long.parseLong(request.getParameter("ppmInstanceId"));

		ResultSet result = connection.createStatement().executeQuery(
				"select database_id, timestamp, type, workflow_element from audittrail_entry where process_instance=" + ppmInstanceId);

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
			if (PromLogger.GROUP_EVENT_END.equals(type) || PromLogger.GROUP_EVENT_START.equals(type)) {
				continue;
			}

			if (AbstractGraphCommand.VSCROLL.equals(type) || AbstractGraphCommand.HSCROLL.equals(type)) {
				continue;
			}
			interactions.add(new InteractionDto(id, timestamp, workflowElement, type));
		}

		writeJson(response, interactions);
	}
}
