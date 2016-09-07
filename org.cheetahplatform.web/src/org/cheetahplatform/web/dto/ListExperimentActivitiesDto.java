package org.cheetahplatform.web.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cheetahplatform.modeler.experiment.IExperimentActivity;
import org.cheetahplatform.modeler.experiment.Workflow;

public class ListExperimentActivitiesDto {
	private String error;
	private Map<Long, List<String>> codeToWorkflowActivities;

	public ListExperimentActivitiesDto() {
		codeToWorkflowActivities = new HashMap<>();
	}

	public void addWorkflow(Workflow workflow) {
		List<String> activities = new ArrayList<String>();
		codeToWorkflowActivities.put(workflow.getId(), activities);
		for (IExperimentActivity activity : workflow.getActivites()) {
			activities.add(activity.getId());
		}
	}

	public Map<Long, List<String>> getCodeToWorkflowActivities() {
		return codeToWorkflowActivities;
	}

	public String getError() {
		return error;
	}

	public void setCodeToWorkflowActivities(Map<Long, List<String>> codeToWorkflowActivities) {
		this.codeToWorkflowActivities = codeToWorkflowActivities;
	}

	public void setError(String error) {
		this.error = error;
	}

}
