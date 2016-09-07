package org.cheetahplatform.web.dto;

public class CredentialsDto {
	private String username;
	private String password;

	public CredentialsDto() {
		// Json
	}

	public CredentialsDto(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
