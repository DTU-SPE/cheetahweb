package org.cheetahplatform.web.synchronize;

import static org.cheetahplatform.common.CommonConstants.ATTRIBUTE_EXPERIMENT_PROCESS_INSTANCE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cheetahplatform.common.CommonConstants;
import org.cheetahplatform.common.logging.Attribute;
import org.cheetahplatform.common.logging.DataContainer;
import org.cheetahplatform.common.logging.db.DatabaseUtil;
import org.cheetahplatform.web.dao.AuditTrailEntryDao;
import org.cheetahplatform.web.dao.ProcessDao;
import org.cheetahplatform.web.dao.ProcessInstanceDao;
import org.cheetahplatform.web.dao.StudyDao;
import org.cheetahplatform.web.dao.SubjectDao;
import org.cheetahplatform.web.dto.PlainAuditTrailEntryDto;
import org.cheetahplatform.web.dto.PlainProcessDto;
import org.cheetahplatform.web.dto.PlainProcessInstanceDto;
import org.cheetahplatform.web.dto.PlainSubjectDto;
import org.cheetahplatform.web.dto.StudyDto;

public class CheetahSynchronizer {
	private Connection sourceConnection;
	private Connection targetConnection;
	private SynchronizeStudiesDto synchronizeRequest;
	private Map<Long, PlainProcessDto> synchronizedFromToProcess;
	private Map<Long, PlainProcessDto> idToProcess;
	private long userId;
	private OverallSynchronizationResultDto result;
	private Map<String, String> oldToNewProcessInstanceIds;

	public CheetahSynchronizer(Connection sourceConnection, Connection targetConnection, SynchronizeStudiesDto synchronizeRequest,
			long userId) {
		this.sourceConnection = sourceConnection;
		this.targetConnection = targetConnection;
		this.synchronizeRequest = synchronizeRequest;
		this.userId = userId;
		this.result = new OverallSynchronizationResultDto();
		this.oldToNewProcessInstanceIds = new HashMap<>();
	}

	/**
	 * Loads and caches processes.
	 *
	 * @throws SQLException
	 */
	private void loadProcesses() throws SQLException {
		ProcessDao processDao = new ProcessDao();
		List<PlainProcessDto> allTargetProcesses = processDao.selectAll(targetConnection);
		synchronizedFromToProcess = new HashMap<>();
		for (PlainProcessDto process : allTargetProcesses) {
			synchronizedFromToProcess.put(process.getSynchronizedFrom(), process);
		}

		List<PlainProcessDto> allSourceProcesses = processDao.selectAll(sourceConnection);
		idToProcess = new HashMap<>();
		for (PlainProcessDto process : allSourceProcesses) {
			idToProcess.put(process.getDatabaseId(), process);
			result.processSynchronized();
		}
	}

	public OverallSynchronizationResultDto synchronize() throws SQLException {
		boolean previousAutoCommit = targetConnection.getAutoCommit();
		targetConnection.setAutoCommit(false);

		try {
			loadProcesses();

			for (StudyDto sourceStudy : synchronizeRequest.getStudies()) {
				// step 1: synchronize the study (either create a new one or reuse if possible)
				StudyDto targetStudy = synchronizeStudy(sourceStudy);

				// step 2: synchronize all subjects from this study
				SubjectDao subjectDao = new SubjectDao();
				List<PlainSubjectDto> sourceSubjects = subjectDao.getSubjectsFor(sourceConnection, sourceStudy.getId());
				List<PlainSubjectDto> targetSubjects = subjectDao.getSubjectsForSynchronizedStudyId(targetConnection,
						targetStudy.getSynchronizedFrom());
				Map<Long, PlainSubjectDto> idToTargetSubject = new HashMap<>();
				for (PlainSubjectDto subject : targetSubjects) {
					idToTargetSubject.put(subject.getSynchronizedFrom(), subject);
				}

				// iterate over all subjects, copy the subject to the target database if not available yet
				for (PlainSubjectDto sourceSubject : sourceSubjects) {
					PlainSubjectDto targetSubject = idToTargetSubject.get(sourceSubject.getId());
					if (targetSubject == null) {
						targetSubject = subjectDao.insertSynchronizedSubject(targetConnection, sourceSubject, targetStudy.getId());
						result.subjecCopied();
					}

					synchronizeProcessInstances(sourceSubject, targetSubject);
					result.subjectSynchronized();
				}
			}

			targetConnection.commit();
		} catch (Exception e) {
			targetConnection.rollback();

			throw e;
		} finally {
			targetConnection.setAutoCommit(previousAutoCommit);
		}

		return result;
	}

	private void synchronizeProcessInstances(PlainSubjectDto sourceSubject, PlainSubjectDto targetSubject) throws SQLException {
		ProcessInstanceDao processInstanceDao = new ProcessInstanceDao();
		List<PlainProcessInstanceDto> sourceProcessInstances = processInstanceDao.getProcessInstancesForSubject(sourceConnection,
				sourceSubject.getId());
		List<PlainProcessInstanceDto> targetProcessInstances = processInstanceDao.getProcessInstancesForSubject(targetConnection,
				targetSubject.getId());
		Map<Long, PlainProcessInstanceDto> synchronizedFromToProcessInstance = new HashMap<>();
		for (PlainProcessInstanceDto instance : targetProcessInstances) {
			synchronizedFromToProcessInstance.put(instance.getSynchronizedFrom(), instance);
		}

		List<PlainProcessInstanceDto> newProcessInstances = new ArrayList<>();
		for (PlainProcessInstanceDto source : sourceProcessInstances) {
			result.processInstanceSynchronized();
			if (synchronizedFromToProcessInstance.containsKey(source.getDatabaseId())) {
				continue; // already copied, ignore
			}

			PlainProcessDto targetProcess = synchronizedFromToProcess.get(source.getProcess());
			if (targetProcess == null) {
				PlainProcessDto toInsert = idToProcess.get(source.getProcess());
				targetProcess = new ProcessDao().insertSynchronized(targetConnection, toInsert);
				synchronizedFromToProcess.put(targetProcess.getSynchronizedFrom(), targetProcess);
				result.processCopied();
			}

			AuditTrailEntryDao auditTrailEntryDao = new AuditTrailEntryDao();
			List<PlainAuditTrailEntryDto> sourceAuditTrailEntries = auditTrailEntryDao
					.getAuditTrailEntriesForProcessInstance(sourceConnection, source.getDatabaseId());

			// keep a mapping of old to new process instance ids (references in audit trail entries)
			PlainProcessInstanceDto targetProcessInstance = processInstanceDao.insertSynchronized(targetConnection, source, targetProcess,
					targetSubject);
			newProcessInstances.add(targetProcessInstance);
			oldToNewProcessInstanceIds.put(source.getId(), targetProcessInstance.getId());

			auditTrailEntryDao.insertSynchronized(targetConnection, sourceAuditTrailEntries, targetProcessInstance.getDatabaseId(),
					targetProcessInstance.getId());
			result.processInstanceCopied();
		}

		// update the references to process instances in the audit trail entries
		AuditTrailEntryDao auditTrailEntryDao = new AuditTrailEntryDao();
		for (PlainProcessInstanceDto processInstance : newProcessInstances) {
			List<PlainAuditTrailEntryDto> entries = auditTrailEntryDao.getAuditTrailEntriesForProcessInstance(targetConnection,
					processInstance.getDatabaseId());

			for (PlainAuditTrailEntryDto entry : entries) {
				if (entry.getType().equals("BPMN_MODELING")) {
					List<Attribute> attributes = DatabaseUtil.fromDataBaseRepresentation(entry.getData());
					List<Attribute> processedAttributes = new ArrayList<>();
					for (Attribute attribute : attributes) {
						if (attribute.getName().equals(CommonConstants.ATTRIBUTE_PROCESS_INSTANCE)) {
							String oldProcessInstanceId = attribute.getContent();
							String newProcessInstanceId = oldToNewProcessInstanceIds.get(oldProcessInstanceId);
							processedAttributes.add(new Attribute(attribute.getName(), newProcessInstanceId));
						} else {
							processedAttributes.add(attribute);
						}
					}

					String data = DatabaseUtil.toDatabaseRepresentation(processedAttributes);
					auditTrailEntryDao.updateDataAttribute(targetConnection, entry, data);
				}
			}

			// also update experiment_process_instance, #426
			String rawData = processInstance.getData();
			DataContainer data = new DataContainer();
			data.addAttributes(DatabaseUtil.fromDataBaseRepresentation(rawData));
			if (data.isAttributeDefined(ATTRIBUTE_EXPERIMENT_PROCESS_INSTANCE)) {
				String oldProcessInstance = data.getAttribute(ATTRIBUTE_EXPERIMENT_PROCESS_INSTANCE);
				String newProcessInstance = oldToNewProcessInstanceIds.get(oldProcessInstance);
				data.setAttribute(ATTRIBUTE_EXPERIMENT_PROCESS_INSTANCE, newProcessInstance);
				String adaptedData = DatabaseUtil.toDatabaseRepresentation(data.getAttributes());
				processInstanceDao.updateDataAttribute(targetConnection, processInstance, adaptedData);
			}
		}
	}

	/**
	 * Synchronizes a given study. Checks if the given study was already synchronized to the target database and if so, returns the
	 * respective study. If the study was not found, a new study is inserted.
	 *
	 * @param study
	 * @return
	 * @throws SQLException
	 */
	private StudyDto synchronizeStudy(StudyDto study) throws SQLException {
		PreparedStatement statement = targetConnection.prepareStatement("select * from study where synchronized_from = ?");
		statement.setLong(1, study.getId());
		ResultSet targetStudyResult = statement.executeQuery();

		StudyDto targetStudy = null;
		if (!targetStudyResult.next()) {
			targetStudy = new StudyDao().insertSynchronizedStudy(targetConnection, study, userId);
			result.studyCopied();
		} else {
			long id = targetStudyResult.getLong("pk_study");
			targetStudy = new StudyDto(id, study.getName(), study.getComment());
			targetStudy.setSynchronizedFrom(study.getId());
		}

		result.studySynchronized();
		statement.close();
		return targetStudy;
	}

}
