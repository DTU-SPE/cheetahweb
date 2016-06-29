package org.cheetahplatform.web.servlet;

import static org.cheetahplatform.web.servlet.AbstractCheetahServlet.readJson;
import static org.cheetahplatform.web.servlet.AbstractCheetahServlet.writeJson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWorkItemStatus;
import org.cheetahplatform.web.CheetahWorker;

public class GetWorkItemStatusServlet extends HttpServlet {
	private static final long serialVersionUID = -3648895729172642515L;

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<Integer> workerIdsAsInteger = readJson(req, ArrayList.class);
		Long[] workerIds = new Long[workerIdsAsInteger.size()];
		for (int i = 0; i < workerIdsAsInteger.size(); i++) {
			workerIds[i] = workerIdsAsInteger.get(i).longValue();
		}

		List<CheetahWorkItemStatus> status = CheetahWorker.determineWorkItemStatus(workerIds);
		writeJson(resp, status);
	}

}
