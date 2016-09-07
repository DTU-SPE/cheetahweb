package org.cheetahplatform.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.eyetracking.cleaning.PupillometryDataFilterRegistry;

public class AvailablePupillometryDataFiltersServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = -314790215219825653L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		writeJson(resp, PupillometryDataFilterRegistry.getFilterDtos());
	}
}
