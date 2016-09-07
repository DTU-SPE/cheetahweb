package org.cheetahplatform.web.dto;

import java.util.ArrayList;
import java.util.List;

public class CommandDto {
	private static final int NO_STEP = -1;

	private int step;
	private long id;
	private long timestamp;

	private String event;
	private List<CommandDto> children;

	public CommandDto(long id, int step, long timestamp, String event) {
		this.step = step;
		this.timestamp = timestamp;
		this.event = event;
		this.children = new ArrayList<CommandDto>();
		this.id = id;
	}

	public CommandDto(long id, long timestamp, String event) {
		this(id, NO_STEP, timestamp, event);
	}

	public void addChild(CommandDto child) {
		children.add(child);
	}

	public List<CommandDto> getChildren() {
		return children;
	}

	public String getEvent() {
		return event;
	}

	public long getId() {
		return id;
	}

	public int getStep() {
		return step;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
