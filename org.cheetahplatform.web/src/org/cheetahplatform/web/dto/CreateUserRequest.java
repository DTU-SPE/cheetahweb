package org.cheetahplatform.web.dto;

public class CreateUserRequest {
	private String email;
	private String firstname;
	private String lastname;
	private String hash;

	public String getEmail() {
		return email;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getHash() {
		return hash;
	}

	public String getLastname() {
		return lastname;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

}
