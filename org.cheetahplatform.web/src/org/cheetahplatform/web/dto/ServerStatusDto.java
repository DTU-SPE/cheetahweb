package org.cheetahplatform.web.dto;

import java.util.ArrayList;
import java.util.List;

public class ServerStatusDto {
	private int workQueueSize;
	private List<CheetahWorkerDto> workers;
	private List<CheetahWorkItemDto> workItems;

	public ServerStatusDto(int workQueueSize) {
		this.workQueueSize = workQueueSize;
		workers = new ArrayList<>();
		workItems = new ArrayList<>();
	}

	public void addWorker(CheetahWorkerDto worker) {
		workers.add(worker);
	}

	public void addWorkItem(CheetahWorkItemDto workItem) {
		workItems.add(workItem);
	}

	public List<CheetahWorkerDto> getWorkers() {
		return workers;
	}

	public List<CheetahWorkItemDto> getWorkItems() {
		return workItems;
	}

	public int getWorkQueueSize() {
		return workQueueSize;
	}

	public void setWorkers(List<CheetahWorkerDto> workers) {
		this.workers = workers;
	}

	public void setWorkQueueSize(int workQueueLenght) {
		this.workQueueSize = workQueueLenght;
	}
}
