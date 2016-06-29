package org.cheetahplatform.web.dto;

public class UserDto {
	public static final String ROLE_ADMINISTRATOR = "administrator";
	public static final String ROLE_USER = "user";

	private String firstname;
	private String lastname;
	private String email;
	private String role;

	public UserDto() {
		// JSON
	}

	public UserDto(String firstname, String lastname, String email, String role) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.role = role;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public String getRole() {
		return role;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
}
