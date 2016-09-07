package org.cheetahplatform.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dao.UserFileDao;

public class GetDefaultUserDataTagsServlet extends AbstractCheetahServlet {
	private static final long serialVersionUID = 4207178748146922605L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		writeJson(resp, UserFileDao.STANDARD_TAGS);
		super.doGet(req, resp);
	}
}
