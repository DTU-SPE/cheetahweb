package org.cheetahplatform.web.eyetracking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cheetahplatform.web.AbstractCheetahWorkItem;
import org.cheetahplatform.web.dto.ConnectRequest;

public abstract class AbstractConnectWorkItem extends AbstractCheetahWorkItem {
	protected ConnectRequest request;

	public AbstractConnectWorkItem(long userId, long fileId, ConnectRequest request, String message) {
		super(userId, fileId, message);
		this.request = request;
	}
}
