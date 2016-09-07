package org.cheetahplatform.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cheetahplatform.common.CommonConstants;
import org.cheetahplatform.common.logging.Attribute;
import org.cheetahplatform.common.logging.db.DatabaseUtil;
import org.cheetahplatform.web.dto.ProcessInstanceDto;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.Driver;

public class ProcessInstanceServlet extends HttpServlet {
	private static final long serialVersionUID = 4379113228610502370L;

	public static Connection getDatabaseConnection() {
		try {
			new Driver();
			return DriverManager.getConnection("jdbc:mysql://localhost/2014_08_pupillometrie", "root", "konami");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection connection = null;

		try {
			connection = getDatabaseConnection();
			ResultSet result = connection
					.createStatement()
					.executeQuery(
							"select database_id, data from process_instance where process_instance.data like '%notationbpmn_1%' and process_instance.database_id < 271;");

			List<ProcessInstanceDto> instances = new ArrayList<ProcessInstanceDto>();
			while (result.next()) {
				long id = result.getLong("database_id");
				String rawData = result.getString("data");
				List<Attribute> attributes = DatabaseUtil.fromDataBaseRepresentation(rawData);
				String experiment = getAttribute(CommonConstants.ATTRIBUTE_EXPERIMENT_PROCESS, attributes);
				String notation = getAttribute(CommonConstants.ATTRIBUTE_NOTATION, attributes);
				String model = getAttribute(CommonConstants.ATTRIBUTE_PROCESS, attributes);

				instances.add(new ProcessInstanceDto(id, experiment, notation, model));
			}

			response.setContentType("application/json");
			JsonFactory factory = new JsonFactory();
			JsonGenerator generator = factory.createGenerator(response.getOutputStream());
			ObjectMapper mapper = new ObjectMapper(factory);
			mapper.writer().writeValue(generator, instances);
			response.getOutputStream().flush();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// ignore
				}
			}
		}
	}

	private String getAttribute(String key, List<Attribute> attributes) {
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals(key)) {
				return attribute.getContent();
			}
		}

		throw new RuntimeException("missing attribute: " + key);
	}

}
