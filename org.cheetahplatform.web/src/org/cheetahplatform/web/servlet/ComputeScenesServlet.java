package org.cheetahplatform.web.servlet;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.web.CheetahWebConstants;
import org.cheetahplatform.web.eyetracking.analysis.PupillometryFileCache;
import org.cheetahplatform.web.eyetracking.cleaning.IPupillometryFileLine;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileColumn;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileHeader;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFileLine;

/**
 * Computes all scenes for a given pupillometry file.
 *
 * @author stefan.zugal
 *
 */
public class ComputeScenesServlet extends AbstractCheetahServlet {

	static class ComputeScenesRequest {
		private long fileId;
		private String timestampColumn;

		public long getFileId() {
			return fileId;
		}

		public String getTimestampColumn() {
			return timestampColumn;
		}

		public void setFileId(long fileId) {
			this.fileId = fileId;
		}

		public void setTimestampColumn(String timestampColumn) {
			this.timestampColumn = timestampColumn;
		}

	}

	private static final long serialVersionUID = -7558992816201030235L;

	@Override
	@SuppressWarnings("unchecked")
	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ComputeScenesRequest computeRequest = readJson(request, ComputeScenesRequest.class);

		PupillometryFile pupillometryFile = PupillometryFileCache.INSTANCE.get(computeRequest.getFileId());
		PupillometryFileHeader header = pupillometryFile.getHeader();
		PupillometryFileColumn timestampColumn = header.getColumn(computeRequest.getTimestampColumn());
		pupillometryFile.collapseEmptyLines(timestampColumn);

		PupillometryFileColumn studioEventDataColumn = header.getColumn(CheetahWebConstants.PUPILLOMETRY_FILE_COLUMN_STUDIO_EVENT_DATA);
		List<String> scenes = new ArrayList<>();
		String previousScene = null;

		for (PupillometryFileLine line : pupillometryFile.getContent()) {
			List<PupillometryFileLine> collapsedLines = (List<PupillometryFileLine>) line.getMarking(PupillometryFile.COLLAPSED_COLUMNS);
			if (collapsedLines == null) {
				continue;
			}

			for (IPupillometryFileLine collapsedLine : collapsedLines) {
				String currentScene = collapsedLine.get(studioEventDataColumn);

				if (previousScene == null) {
					scenes.add(currentScene);
					previousScene = currentScene;
				} else if (previousScene.equals(currentScene)) {
					previousScene = null;
				}
			}
		}

		writeJson(response, scenes);
	}
}
