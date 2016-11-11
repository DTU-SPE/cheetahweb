package org.cheetahplatform.web.eyetracking.analysis;

/**
 * Represents a single data processing step, e.g., applying a series of filters.
 *
 * @author stefan.zugal
 *
 */
public class DataProcessingStep {
	public static final String DATA_PROCESSING_TYPE_ANALYZE = "analyze";
	public static final String DATA_PROCESSING_TYPE_CLEAN = "clean";

	private long id;
	private String name;
	private String type;
	private int version;
	private String configuration;

	public DataProcessingStep(long id, String name, String type, int version, String configuration) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.version = version;
		this.configuration = configuration;
	}

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
