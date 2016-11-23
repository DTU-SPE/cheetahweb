package org.cheetahplatform.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.eyetracking.analysis.steps.AnalyzeStepType;

/**
 * Servlet implementation class ListAnalyzeTypesServlet
 */
public class ListAnalyzeTypesServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -2361442789540290985L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		writeJson(response, AnalyzeStepType.ALL);
	}

}
