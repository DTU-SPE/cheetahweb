package org.cheetahplatform.web.servlet;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.StudyDao;
import org.cheetahplatform.web.synchronize.AbstractSynchronizeServlet;

public class AnalyzeDatabaseForSynchronizationServlet extends AbstractSynchronizeServlet {
	private static final long serialVersionUID = -4367565269665848614L;

	@Override
	protected void doPostWith(Connection sourceConnection, Connection targetConnection, HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		writeJson(resp, new StudyDao().getStudies(sourceConnection));
	}

}
