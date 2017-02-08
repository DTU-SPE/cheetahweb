package org.cheetahplatform.web.eyetracking.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.eyetracking.cleaning.PupillometryFile;

/**
 * A simple cache for storing pupillometry files. Be aware of potential side effects when changing a file!
 *
 * @author Jakob
 */
public class PupillometryFileCache {
	private class PupillometryFileEntry {
		private PupillometryFile file;
		private Date date;

		public PupillometryFileEntry(PupillometryFile file, Date date) {
			super();
			this.file = file;
			this.date = date;
		}

		public Date getDate() {
			return date;
		}

		public PupillometryFile getFile() {
			return file;
		}
	}

	private static final int CAPACITY = 2;

	public static final PupillometryFileCache INSTANCE = new PupillometryFileCache();

	private Map<Long, PupillometryFileEntry> cache;

	private PupillometryFileCache() {
		this.cache = new ConcurrentHashMap<Long, PupillometryFileEntry>();
	}

	public PupillometryFile get(long userFile) throws SQLException, FileNotFoundException {
		PupillometryFileEntry cached = cache.remove(userFile);
		if (cached != null) {
			cache.put(userFile, new PupillometryFileEntry(cached.getFile(), new Date()));
			return cached.getFile();
		}

		if (cache.size() >= CAPACITY) {
			Long oldestId = null;

			for (Entry<Long, PupillometryFileEntry> entry : cache.entrySet()) {
				PupillometryFileEntry cachedFile = entry.getValue();
				if (oldestId == null) {
					oldestId = entry.getKey();
					continue;
				}

				PupillometryFileEntry currentOldest = cache.get(oldestId);
				if (cachedFile.getDate().before(currentOldest.getDate())) {
					oldestId = entry.getKey();
				}
			}
			if (oldestId != null) {
				cache.remove(oldestId);
			}
		}

		UserFileDao userFileDao = new UserFileDao();
		String path = userFileDao.getPath(userFile);
		File rawFile = userFileDao.getUserFile(path);

		PupillometryFile pupillometryFile = new PupillometryFile(rawFile, PupillometryFile.SEPARATOR_TABULATOR, true, ".");
		cache.put(userFile, new PupillometryFileEntry(pupillometryFile, new Date()));

		return pupillometryFile;
	}
}
