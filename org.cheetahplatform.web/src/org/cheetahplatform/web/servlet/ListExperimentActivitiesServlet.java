package org.cheetahplatform.web.servlet;

import static org.cheetahplatform.web.servlet.AbstractCheetahServlet.writeJson;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.modeler.ExperimentProvider;
import org.cheetahplatform.modeler.experiment.IExperiment;
import org.cheetahplatform.modeler.experiment.Workflow;
import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.ListExperimentActivitiesDto;
import org.cheetahplatform.web.dto.ListExperimentActivitiesRequest;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.util.FileUtils;

public class ListExperimentActivitiesServlet extends HttpServlet {

	private static final long serialVersionUID = 1235410872993342499L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ListExperimentActivitiesRequest requestDto = AbstractCheetahServlet.readJson(request, ListExperimentActivitiesRequest.class);
		UserFileDao userFileDao = new UserFileDao();
		ListExperimentActivitiesDto responseDto = new ListExperimentActivitiesDto();
		Set<IExperiment> addedExperiments = new HashSet<>();

		try {
			for (Long id : requestDto.getFiles()) {
				UserFileDto file = userFileDao.getFile(id);
				String filename = FileUtils.getFileNameWithoutExtension(file.getFilename());
				String[] tokens = filename.split(CheetahWebConstants.FILENAME_PATTERN_SEPARATOR);
				if (tokens == null) {
					responseDto.setError("Could not parse filename " + filename + ". Please make sure it contains subject@experiment");
					break;
				}

				String experimentId = tokens[1];
				IExperiment experiment = ExperimentProvider.getExperiment(experimentId);
				if (experiment == null) {
					responseDto.setError("Could not find experiment " + experimentId
							+ ". Please make sure the experiment's jar is available under WEB-INF/lib.");
					break;
				}

				addedExperiments.add(experiment);
				if (addedExperiments.size() > 1) {
					Iterator<IExperiment> experiments = addedExperiments.iterator();
					responseDto.setError("Found two different experiments: " + experiments.next().getExperimentProcess().getId() + " and "
							+ experiments.next().getExperimentProcess().getId() + ". Please select only one experiment at a time.");
					break;
				}

				for (Workflow workflow : experiment.getWorkflows()) {
					responseDto.addWorkflow(workflow);
				}
			}
		} catch (SQLException e) {
			responseDto.setError(e.getMessage());
		}

		writeJson(response, responseDto);
	}

}
