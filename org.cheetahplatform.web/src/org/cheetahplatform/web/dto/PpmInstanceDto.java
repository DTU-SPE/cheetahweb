package org.cheetahplatform.web.dto;

public class PpmInstanceDto {

	private long processInstanceId;
	private String process;
	private long processId;

	public PpmInstanceDto(long processInstanceId, String processName, long processId) {
		this.processInstanceId = processInstanceId;
		this.process = processName;
		this.processId = processId;
	}

	public String getProcess() {
		return process;
	}

	public long getProcessId() {
		return processId;
	}

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public void setProcessId(long processId) {
		this.processId = processId;
	}

	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

}
