package org.cheetahplatform.web.synchronize;

public class SingleSynchronziationResultDto {
	private String name;
	private int synchronizedElements;
	private int copiedElements;

	public SingleSynchronziationResultDto(String name) {
		this.name = name;
	}

	public int getCopiedElements() {
		return copiedElements;
	}

	public String getName() {
		return name;
	}

	public int getSynchronizedElements() {
		return synchronizedElements;
	}

	public void increaseCopied() {
		copiedElements++;
	}

	public void increaseSynchronized() {
		synchronizedElements++;
	}

	public void setCopiedElements(int copiedElements) {
		this.copiedElements = copiedElements;
	}

	public void setSynchronizedElements(int synchronizedElements) {
		this.synchronizedElements = synchronizedElements;
	}
}
