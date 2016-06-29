package org.cheetahplatform.web.synchronize;

import java.util.Arrays;
import java.util.List;

public class OverallSynchronizationResultDto {
	private SingleSynchronziationResultDto studies;
	private SingleSynchronziationResultDto subjects;
	private SingleSynchronziationResultDto processes;
	private SingleSynchronziationResultDto processInstances;

	public OverallSynchronizationResultDto() {
		studies = new SingleSynchronziationResultDto("Studies");
		subjects = new SingleSynchronziationResultDto("Subjects");
		processInstances = new SingleSynchronziationResultDto("Process Instances");
		processes = new SingleSynchronziationResultDto("Processes");
	}

	public List<SingleSynchronziationResultDto> getAll() {
		return Arrays.asList(studies, subjects, processInstances);
	}

	public SingleSynchronziationResultDto getProcesses() {
		return processes;
	}

	public SingleSynchronziationResultDto getProcessInstances() {
		return processInstances;
	}

	public SingleSynchronziationResultDto getStudies() {
		return studies;
	}

	public SingleSynchronziationResultDto getSubjects() {
		return subjects;
	}

	public void processCopied() {
		processes.increaseCopied();
	}

	public void processInstanceCopied() {
		processInstances.increaseCopied();
	}

	public void processInstanceSynchronized() {
		processInstances.increaseSynchronized();
	}

	public void processSynchronized() {
		processes.increaseSynchronized();
	}

	public void studyCopied() {
		studies.increaseCopied();
	}

	public void studySynchronized() {
		studies.increaseSynchronized();
	}

	public void subjecCopied() {
		subjects.increaseCopied();
	}

	public void subjectSynchronized() {
		subjects.increaseSynchronized();
	}
}
