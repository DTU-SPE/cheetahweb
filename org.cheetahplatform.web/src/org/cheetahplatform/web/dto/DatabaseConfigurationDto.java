package org.cheetahplatform.web.dto;

public class DatabaseConfigurationDto {
	private long id;
	private String host;
	private int port;
	private String schema;
	private String password;
	private String username;

	public DatabaseConfigurationDto() {
	}

	public DatabaseConfigurationDto(long id, String host, int port, String schema, String password, String username) {
		this.id = id;
		this.host = host;
		this.port = port;
		this.schema = schema;
		this.password = password;
		this.username = username;
	}

	public String asMysqlUrl() {
		return "jdbc:mysql://" + host + ":" + port + "/" + schema;
	}

	public String getHost() {
		return host;
	}

	public long getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}

	public String getSchema() {
		return schema;
	}

	public String getUsername() {
		return username;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setPassword(String user) {
		this.password = user;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
