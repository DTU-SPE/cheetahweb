package org.cheetahplatform.web.servlet;

import java.util.List;
import java.util.Map;

import org.cheetahplatform.web.AbstractCheetahWorkItem;
import org.cheetahplatform.web.dao.SubjectDao;
import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.ReportableResult;
import org.cheetahplatform.web.dto.ReportableResultEntry;
import org.cheetahplatform.web.dto.SubjectDto;
import org.cheetahplatform.web.dto.UserFileDto;
import org.cheetahplatform.web.eyetracking.CheetahWorkItemGuard;
import org.cheetahplatform.web.eyetracking.analysis.DataProcessing;
import org.cheetahplatform.web.eyetracking.analysis.ITrialAnalyzer;
import org.cheetahplatform.web.eyetracking.analysis.steps.AnalyzeConfiguration;
import org.cheetahplatform.web.eyetracking.analysis.steps.TrialAnalyzerFactory;
import org.cheetahplatform.web.eyetracking.cleaning.DataProcessingContext;
import org.cheetahplatform.web.eyetracking.cleaning.IDataProcessingWorkItem;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;

public class AnalyzeTrialsWorkItem extends AbstractCheetahWorkItem implements IDataProcessingWorkItem {
	private ITrialAnalyzer analyzer;
	private CheetahWorkItemGuard guard;

	public AnalyzeTrialsWorkItem(long userId, long fileId, AnalyzeConfiguration config, CheetahWorkItemGuard guard,
			DataProcessing processing) {
		super(userId, fileId, "Analyzing trials.");
		this.guard = guard;

		analyzer = TrialAnalyzerFactory.createAnalyzer(config, processing);
	}

	@Override
	public void doWork() throws Exception {
		throw new UnsupportedOperationException("Analysing trials is only implemented for study data processing.");
	}

	@Override
	public boolean doWork(PupillometryFile file, DataProcessingContext context) throws Exception {
		Map<String, List<ReportableResultEntry>> results = analyzer.analyze(context.getTrialEvaluation(), file);

		UserFileDto userFileDto = new UserFileDao().getFile(fileId);
		Long subjectId = userFileDto.getSubjectId();
		SubjectDto subject = new SubjectDao().getSubjectWithId(userId, subjectId);
		String subjectName = subject.getSubjectName();

		ReportableResult reportableResult = new ReportableResult(subjectName);
		reportableResult.putAllResults(results);
		guard.reportResult(reportableResult);

		return true;
	}
}
