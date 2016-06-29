package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahServletContextListener;
import org.cheetahplatform.web.CheetahWorker;
import org.cheetahplatform.web.dto.CheetahWorkItemDto;
import org.cheetahplatform.web.dto.ServerStatusDto;

public class ServerStatusServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -2403381788213384353L;

	@Override
	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		super.doGetWithDatabaseConnection(connection, request, response);
		long userId = getUserId(connection, request);

		int workQueueSize = CheetahWorker.getWorkQueueSize();
		ServerStatusDto result = new ServerStatusDto(workQueueSize);
		List<CheetahWorker> workers = CheetahServletContextListener.getWorkers();
		for (CheetahWorker cheetahWorker : workers) {
			result.addWorker(cheetahWorker.getMessage());
		}

		List<CheetahWorkItemDto> workItem = CheetahWorker.getScheduledWorkItems(userId);
		for (CheetahWorkItemDto cheetahWorkItemDto : workItem) {
			result.addWorkItem(cheetahWorkItemDto);
		}

		writeJson(response, result);
	}
}
