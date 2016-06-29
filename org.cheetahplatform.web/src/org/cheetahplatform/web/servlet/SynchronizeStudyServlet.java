package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.dto.DatabaseConfigurationDto;
import org.cheetahplatform.web.synchronize.AbstractSynchronizeServlet;
import org.cheetahplatform.web.synchronize.CheetahSynchronizer;
import org.cheetahplatform.web.synchronize.OverallSynchronizationResultDto;
import org.cheetahplatform.web.synchronize.SynchronizeStudiesDto;

import com.fasterxml.jackson.core.JsonParseException;

public class SynchronizeStudyServlet extends AbstractSynchronizeServlet {
	private static final long serialVersionUID = 1L;

	private SynchronizeStudiesDto currentRequest;

	@Override
	protected void doPostWith(Connection sourceConnection, Connection targetConnection, HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		long userId = getUserId(targetConnection, req);
		OverallSynchronizationResultDto result = new CheetahSynchronizer(sourceConnection, targetConnection, currentRequest, userId)
				.synchronize();
		writeJson(resp, result);
	}

	@Override
	protected DatabaseConfigurationDto getConfiguration(HttpServletRequest req) throws JsonParseException, IOException {
		currentRequest = readJson(req, SynchronizeStudiesDto.class);
		return currentRequest.getConfiguration();
	}

}
