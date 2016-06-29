package org.cheetahplatform.web.migration;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;

public class UserCreationInitTask implements CustomSqlChange {

	public List<RawSqlStatement> assembleStatement(String firstname, String lastname, String email, String passwordHash) {
		String userQuery = "INSERT INTO user_table (firstname, lastname, email, password) VALUES ('" + firstname + "','" + lastname + "','"
				+ email + "','" + passwordHash + "')";

		List<RawSqlStatement> statements = new ArrayList<>();
		statements.add(new RawSqlStatement(userQuery));

		String userPrimaryKeyQuery = "SELECT @tmp:= pk_user from user_table ut where ut.email='" + email + "'";
		String roleQuery = "INSERT INTO user_roles ( email, role, fk_user) VALUES ('" + email + "','administrator', @tmp)";
		statements.add(new RawSqlStatement(userPrimaryKeyQuery));
		statements.add(new RawSqlStatement(roleQuery));

		return statements;
	}

	@Override
	public SqlStatement[] generateStatements(Database database) throws CustomChangeException {
		List<RawSqlStatement> statements = new ArrayList<>();
		statements.addAll(assembleStatement("Admin", "", "admin@cheetahplatform.org", "43d52a2882cf8947e927f9a3cd79f11f"));
		return statements.toArray(new RawSqlStatement[statements.size()]);
	}

	@Override
	public String getConfirmationMessage() {
		return "User list initialized";
	}

	@Override
	public void setFileOpener(ResourceAccessor arg0) {
		// nothing to do

	}

	@Override
	public void setUp() throws SetupException {
		// nothing to do
	}

	@Override
	public ValidationErrors validate(Database arg0) {
		return null;
	}

}
