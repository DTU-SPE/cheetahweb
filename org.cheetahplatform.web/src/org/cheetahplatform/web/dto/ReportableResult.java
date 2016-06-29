package org.cheetahplatform.web.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ReportableResult {
	private String identifier;
	private Map<String, String> results;

	public ReportableResult(String identifier) {
		this.identifier = identifier;
		results = new HashMap<String, String>();
	}

	public void addAllResults(Map<String, String> toInsert) {
		results.putAll(toInsert);
	}

	public void addResult(String key, String value) {
		results.put(key, value);
	}

	public Set<String> getDefinedKeys() {
		return results.keySet();
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getResult(String key) {
		return results.get(key);
	}

	public Map<String, String> getResults() {
		return Collections.unmodifiableMap(results);
	}

	public boolean isResultDefined(String key) {
		return results.containsKey(key);
	}
}
