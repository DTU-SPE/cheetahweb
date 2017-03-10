package org.cheetahplatform.web.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.cheetahplatform.common.CommonConstants;
import org.cheetahplatform.common.logging.ProcessInstance;
import org.cheetahplatform.common.logging.db.DatabasePromReader;
import org.cheetahplatform.modeler.DefaultConfiguration;
import org.cheetahplatform.modeler.NotationProvider;
import org.cheetahplatform.modeler.bpmn.BpmnNotation;
import org.cheetahplatform.modeler.editor.GraphCommandStack;
import org.cheetahplatform.modeler.model.Graph;
import org.cheetahplatform.modeler.model.INotation;
import org.cheetahplatform.modeler.replay.CommandReplayer;
import org.cheetahplatform.web.dao.MovieDao;
import org.cheetahplatform.web.dao.UserDao;
import org.cheetahplatform.web.dto.MovieDto;
import org.cheetahplatform.web.eyetracking.CachedEyeTrackingData;
import org.cheetahplatform.web.eyetracking.DatabaseEyeTrackingSource;
import org.cheetahplatform.web.eyetracking.EyeTrackingCache;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.Driver;

public abstract class AbstractCheetahServlet extends HttpServlet {

	private static final long serialVersionUID = 2259807692138437863L;

	static {
		try {
			new Driver();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void closeQuietly(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// ignore
			}
		}
	}

	public static CommandReplayer createReplayer(int id) throws SQLException {
		ProcessInstance instance = readPpmInstance(id);
		return createReplayer(instance);
	}

	public static CommandReplayer createReplayer(long id, Connection connection) throws SQLException {
		ProcessInstance instance = DatabasePromReader.readProcessInstance(id, connection);
		return createReplayer(instance);
	}

	public static CommandReplayer createReplayer(ProcessInstance instance) {
		INotation notation = NotationProvider.getNotation(BpmnNotation.ID);
		if (notation == null) {
			notation = new BpmnNotation();
			NotationProvider.registerNotation(notation);
		}

		DefaultConfiguration configuration = new DefaultConfiguration();
		notation.setDefaultConfigurationValues(configuration);
		Graph graph = new Graph(notation.getAllDescriptors(configuration));

		return new CommandReplayer(new GraphCommandStack(graph), graph, instance, configuration);
	}

	public static Connection getDatabaseConnection() throws SQLException {
		DataSource ds;
		try {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			ds = (DataSource) envContext.lookup("jdbc/cheetahweb");
			return ds.getConnection();
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> T readJson(HttpServletRequest request, Class<T> clazz) throws JsonParseException, IOException {
		return readJson(request.getInputStream(), clazz);
	}

	public static <T> T readJson(InputStream input, Class<T> clazz) throws JsonParseException, IOException {
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);
		factory.setCodec(mapper);
		JsonParser parser = factory.createParser(input);
		return parser.readValueAs(clazz);
	}

	public static <T> T readJson(String json, Class<T> clazz) throws JsonParseException, IOException {
		return readJson(new ByteArrayInputStream(json.getBytes()), clazz);
	}

	public static ProcessInstance readPpmInstance(long id) throws SQLException {
		ProcessInstance instance = DatabasePromReader.readProcessInstance(id, getDatabaseConnection());
		return instance;
	}

	public static void writeJson(HttpServletResponse response, Object toWrite)
			throws JsonGenerationException, JsonMappingException, IOException {
		writeJson(response.getOutputStream(), toWrite);
		response.getOutputStream().flush();
	}

	public static String writeJson(Object toWrite) throws JsonGenerationException, JsonMappingException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		writeJson(output, toWrite);

		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	public static void writeJson(OutputStream output, Object toWrite) throws JsonGenerationException, JsonMappingException, IOException {
		JsonFactory factory = new JsonFactory();
		JsonGenerator generator = factory.createGenerator(output);
		ObjectMapper mapper = new ObjectMapper(factory);
		mapper.writer().writeValue(generator, toWrite);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Connection connection = null;
		try {
			connection = getDatabaseConnection();
			doGetWithDatabaseConnection(connection, req, resp);
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

	protected void doGetWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		// nothing to do
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Connection connection = null;
		try {
			connection = getDatabaseConnection();
			doPostWithDatabaseConnection(connection, req, resp);
		} catch (Exception e) {
			e.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			closeQuietly(connection);
		}
	}

	protected void doPostWithDatabaseConnection(Connection connection, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// subclasses may override
	}

	protected long getSessionStart(long ppmInstanceId) throws Exception {
		long startTimestamp = Long.MAX_VALUE;
		List<MovieDto> movies = new MovieDao().getMovieForPpmInstance(ppmInstanceId);
		for (MovieDto movieDto : movies) {
			if (movieDto.getStartTimestamp() < startTimestamp) {
				startTimestamp = movieDto.getStartTimestamp();
			}
		}

		List<CachedEyeTrackingData> cachedData = EyeTrackingCache.INSTANCE.getForPpmInstance(ppmInstanceId,
				new DatabaseEyeTrackingSource());
		for (CachedEyeTrackingData cachedEyeTrackingData : cachedData) {
			long sessionStart = cachedEyeTrackingData.getSessionStart();
			if (sessionStart < startTimestamp) {
				startTimestamp = sessionStart;
			}
		}

		Connection connection = AbstractCheetahServlet.getDatabaseConnection();
		ProcessInstance processInstance = DatabasePromReader.readProcessInstance(ppmInstanceId, connection);
		connection.close();

		String timestampAttribute = processInstance.getAttributeSafely(CommonConstants.ATTRIBUTE_TIMESTAMP);
		if (timestampAttribute != null) {
			// convert to nanos
			long parsedTimestamp = Long.parseLong(timestampAttribute) * 1000;
			if (parsedTimestamp < startTimestamp) {
				startTimestamp = parsedTimestamp;
			}
		}

		return startTimestamp;
	}

	protected long getUserId(Connection connection, HttpServletRequest request) throws SQLException {
		UserDao userDao = new UserDao();
		return userDao.getUserId(connection, request);
	}
}
