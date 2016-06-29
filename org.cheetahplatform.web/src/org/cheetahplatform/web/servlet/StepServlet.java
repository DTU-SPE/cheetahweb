package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.common.logging.DatabaseAuditTrailEntry;
import org.cheetahplatform.modeler.replay.CommandDelegate;
import org.cheetahplatform.modeler.replay.CommandReplayer;
import org.cheetahplatform.web.dto.CommandDto;

/**
 * Servlet implementation class StepServlet
 */
public class StepServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int id = Integer.parseInt(request.getParameter("processInstance"));
		Connection connection = null;

		try {
			connection = AbstractCheetahServlet.getDatabaseConnection();
			CommandReplayer replayer = createReplayer(id, connection);
			List<CommandDelegate> commands = replayer.getCommands();

			List<CommandDto> dtos = new ArrayList<CommandDto>();
			long startTimetamp = commands.get(0).getAuditTrailEntry().getTimestamp().getTime();
			for (int i = 0; i < commands.size(); i++) {
				CommandDelegate command = commands.get(i);
				DatabaseAuditTrailEntry entry = (DatabaseAuditTrailEntry) command.getAuditTrailEntry();
				long timestamp = entry.getTimestamp().getTime() - startTimetamp;
				CommandDto commandDto = new CommandDto(entry.getId(), (i + 1), timestamp, command.getLabel());
				dtos.add(commandDto);

				for (CommandDelegate childCommand : command.getChildren()) {
					long childId = ((DatabaseAuditTrailEntry) childCommand.getAuditTrailEntry()).getId();
					commandDto.addChild(new CommandDto(childId, timestamp, childCommand.getLabel()));
				}
			}

			AbstractCheetahServlet.writeJson(response, dtos);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// ignore
			}
		}

	}
}
