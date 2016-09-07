package org.cheetahplatform.web.eyetracking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.cheetahplatform.web.dao.UserFileDao;
import org.cheetahplatform.web.dto.UserFileDto;

public class EyeTrackingCache {
	private static final int CAPACITY = 20;
	public static final EyeTrackingCache INSTANCE = new EyeTrackingCache();

	private Map<Long, CachedEyeTrackingData> cache;

	private EyeTrackingCache() {
		this.cache = new ConcurrentHashMap<Long, CachedEyeTrackingData>();
	}

	public List<CachedEyeTrackingData> get(IEyeTrackingDataSource source, Long... userFiles) throws Exception {
		List<CachedEyeTrackingData> result = new ArrayList<>();
		for (Long userFile : userFiles) {
			result.add(get(userFile, source));
		}

		return result;
	}

	public List<CachedEyeTrackingData> get(IEyeTrackingDataSource source, UserFileDto... userFiles) throws Exception {
		List<CachedEyeTrackingData> result = new ArrayList<>();
		for (UserFileDto userFile : userFiles) {
			CachedEyeTrackingData data = get(userFile.getId(), source);
			result.add(data);
		}

		return result;
	}

	private CachedEyeTrackingData get(long userFile, IEyeTrackingDataSource source) throws Exception {
		CachedEyeTrackingData cached = cache.get(userFile);
		if (cached != null) {
			return cached;
		}

		if (cache.size() > CAPACITY) {
			CachedEyeTrackingData oldest = null;
			Long oldestId = null;

			Set<Entry<Long, CachedEyeTrackingData>> entrySet = cache.entrySet();
			for (Entry<Long, CachedEyeTrackingData> entry : entrySet) {
				if (entry.getValue() == null) {
					oldestId = entry.getKey();
					break;
				}
				CachedEyeTrackingData cachedEyeTrackingData = entry.getValue();
				if (oldest == null || cachedEyeTrackingData.isOlderThan(oldest)) {
					oldest = cachedEyeTrackingData;
					oldestId = entry.getKey();
				}
			}
			if (oldestId != null) {
				cache.remove(oldestId);
			}
		}

		cached = source.load(userFile);
		cache.put(userFile, cached);

		return cached;
	}

	public List<CachedEyeTrackingData> getForPpmInstance(long ppmInstanceId, IEyeTrackingDataSource source) throws Exception {
		UserFileDao userFileDao = new UserFileDao();
		List<UserFileDto> files = userFileDao.getEyeTrackingDataForPpmInstance(ppmInstanceId);
		if (files.isEmpty()) {
			throw new NoEyeTrackingDataException(
					"Could not find any connected pupillometric data for PPM instance with id '" + ppmInstanceId + "'.");
		}

		return get(source, files.toArray(new UserFileDto[files.size()]));
	}

	public void invalidateCache(long id) {
		cache.remove(id);
	}
}
