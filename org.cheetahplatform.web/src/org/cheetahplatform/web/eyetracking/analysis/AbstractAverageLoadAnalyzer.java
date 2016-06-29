package org.cheetahplatform.web.eyetracking.analysis;

import org.cheetahplatform.web.dao.SubjectDao;
import org.cheetahplatform.web.dto.SubjectDto;

public abstract class AbstractAverageLoadAnalyzer implements IEyeTrackingDataAnalyzer {
	protected long ppmInstanceId;
	protected long userId;

	public AbstractAverageLoadAnalyzer(long ppmInstanceId, long userId) {
		this.ppmInstanceId = ppmInstanceId;
		this.userId = userId;
	}

	@Override
	public String getHeader() {
		return "Data Label;Average Load Left Pupil;Average Load Right Pupil;";
	}

	@Override
	public String getSubjectIdentifier() throws Exception {
		SubjectDao subjectDao = new SubjectDao();
		SubjectDto subject = subjectDao.getSubjectForPpmInstanceId(userId, ppmInstanceId);
		return subject.getSubjectName();
	}
}
