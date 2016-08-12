package org.cheetahplatform.web.eyetracking.analysis;

/**
 * Represents a single data processing step, e.g., applying a series of filters.
 *
 * @author stefan.zugal
 *
 */
public class DataProcessingStep {
	private long id;
	private String name;
	private String type;
	private int version;
	private String configuration;

	public String getConfiguration() {
		return configuration;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getVersion() {
		return version;
	}

}
