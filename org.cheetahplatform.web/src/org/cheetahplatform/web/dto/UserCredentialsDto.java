package org.cheetahplatform.web.dto;

public class UserCredentialsDto {
	private String email;
	private String oldHash;
	private String newHash;

	public UserCredentialsDto() {
		// JSON
	}

	public String getEmail() {
		return email;
	}

	public String getNewHash() {
		return newHash;
	}

	public String getOldHash() {
		return oldHash;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setNewHash(String newHash) {
		this.newHash = newHash;
	}

	public void setOldHash(String oldHash) {
		this.oldHash = oldHash;
	}
}
