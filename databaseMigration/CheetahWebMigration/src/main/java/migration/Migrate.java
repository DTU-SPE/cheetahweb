package migration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cheetahplatform.common.logging.db.DatabasePromReader;
import org.cheetahplatform.common.ui.dialog.ProcessInstanceDatabaseHandle;

import com.google.gson.Gson;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.cheetahplatform.common.logging.Attribute;
import org.cheetahplatform.common.logging.AuditTrailEntry;
import  org.cheetahplatform.common.logging.Process;
import org.cheetahplatform.common.logging.ProcessInstance;


public class Migrate {
	
	private static SimpleDateFormat logDateFormat = new SimpleDateFormat("hh:mm:ss.S");
	private static Map<String, Class<?>> forcedTypes = new HashMap<String, Class<?>>();
	
	public static void main(String[] args) throws SQLException, FileNotFoundException {
		log("=== Starting migration ===");
		
		// forced types set up
		logStart("Setting up forced types");
		forcedTypes.put("name", String.class);
		logDone();
		
		logStart("Database connection");
		Connection connectionNoDbSelection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "cheetah_web", "cheetah_web");
		Connection connectionOld = DriverManager.getConnection("jdbc:mysql://localhost:3306/cheetah_web_old", "cheetah_web", "cheetah_web");
		Connection connectionNew = DriverManager.getConnection("jdbc:mysql://localhost:3306/cheetah_web", "cheetah_web", "cheetah_web");
		logDone();
		
		logStart("Truncating old event tables");
		ScriptRunner sr1 = new ScriptRunner(connectionNew);
		sr1.runScript(new BufferedReader(new FileReader("resources/cleanup.sql")));
//		connectionNew.prepareStatement("delete from events").execute();
//		connectionNew.prepareStatement("delete from time_phases").execute();
		logDone();
		
		logStart("Running SQL import script");
		ScriptRunner sr2 = new ScriptRunner(connectionNoDbSelection);
		sr2.runScript(new BufferedReader(new FileReader("resources/from_old_to_new.sql")));
		logDone();
		
		for (ProcessInstanceDatabaseHandle pidh : loadAllProcessInstances(connectionOld)) {
			ProcessInstance pi = DatabasePromReader.readProcessInstance(pidh.getDatabaseId(), connectionOld);
			// each process instance is actually a time phase
			for(AuditTrailEntry ate : pi.getEntries()) {
				
				boolean isTimePhase = (!pi.getAttributeSafely("type").equalsIgnoreCase("bpmn")) && (!pi.getAttributeSafely("type").equalsIgnoreCase("bpmn_1.0"));
				long fk_subject = databaseIdToSubjectId(pidh.getDatabaseId(), connectionOld);
				
				if (isTimePhase) {
					
					logStart("Adding timephase");
					List<Attribute> attributes = ate.getAttributes();
					attributes.addAll(pi.getAttributes());
					// this audit trail entry is actually a time phase
					PreparedStatement statement = connectionNew.prepareStatement("insert into time_phases (fk_subject, type, name, time_start, time_end, attributes) values (?, ?, ?, ?, ?, ?)");
					statement.setLong(1, fk_subject);
					statement.setString(2, ate.getEventType());
					statement.setString(3, ate.getAttributeSafely("experiment_activity_id"));
					statement.setLong(4, pi.getLongAttribute("timestamp"));
					statement.setLong(5, pi.getEntries().get(pi.getEntries().size() - 1).getTimestamp().getTime());
					statement.setString(6, attributeListToJSon(attributes));
					statement.execute();
					statement.close();
					logDone();
					
					
					logStart("Add session_videos");
					long processInstanceId = Long.parseLong(pi.getId());
					long idLastTimePhases = getIdOfLastTimePhase(connectionNew);
					
					PreparedStatement statement2 = connectionOld.prepareStatement("select * from eyetracking_movie where process_instance = ?");
					statement2.setLong(1, processInstanceId);
					ResultSet resultSet = statement2.executeQuery();
					while (resultSet.next()) {
						PreparedStatement statement3 = connectionNew.prepareStatement("insert into session_videos (fk_subject, fk_time_phase, movie_path, movie_type, start_timestamp) values (?, ?, ?, ?, ?)");
						statement3.setLong(1, resultSet.getLong("fk_subject"));
						statement3.setLong(2, idLastTimePhases);
						statement3.setString(3, resultSet.getString("movie_path"));
						statement3.setString(4, resultSet.getString("movie_type"));
						statement3.setLong(5, resultSet.getLong("start_timestamp"));
						statement3.execute();
						statement3.close();
					}
					statement2.close();
					logDone();
					
				} else {
					
					// this audit trail entry is actually an event
					Long timeStart = null;
					Long timeEnd = ate.getTimestamp().getTime();
					if (!ate.getAttributeSafely("add_node_start_time").isEmpty()) {
						timeStart = ate.getLongAttribute("add_node_start_time");
					}
					if (!ate.getAttributeSafely("rename_start_time").isEmpty()) {
						timeStart = ate.getLongAttribute("rename_start_time");
					}
					
					String eventName = null;
					
					if ("SELECT_TOOL".equals(ate.getEventType())) {
						eventName = ate.getAttribute("tool_name");
					}
					
					if ("CREATE_NODE".equals(ate.getEventType()) ||
							"MOVE_NODE".equals(ate.getEventType()) ||
							"MOVE_EDGE_LABEL".equals(ate.getEventType()) ||
							"DELETE_NODE".equals(ate.getEventType())) {
						eventName = ate.getAttribute("name");
					}
					
					if ("RENAME".equals(ate.getEventType())) {
						eventName = "\"" + ate.getAttribute("name") + "\" -> \"" + ate.getAttribute("new_name") + "\"";
					}
					
					if ("CREATE_EDGE".equals(ate.getEventType()) ||
							"DELETE_EDGE".equals(ate.getEventType())) {
						eventName = "\"" + ate.getAttribute("source_node_name") + "\" -> \"" + ate.getAttribute("target_node_name") + "\"";
					}
					
					PreparedStatement statement = connectionNew.prepareStatement("insert into events (time_start, time_end, type, name, attributes, fk_subject) values (?, ?, ?, ?, ?, ?)");
					statement.setObject(1, timeStart);
					statement.setLong(2, timeEnd);
					statement.setString(3, ate.getEventType());
					statement.setString(4, eventName);
					statement.setString(5, attributeListToJSon(ate.getAttributes()));
					statement.setLong(6, fk_subject);
					statement.execute();
					statement.close();
				}
			}
		}
		
		logStart("Database diconnection");
		connectionNoDbSelection.close();
		connectionOld.close();
		connectionNew.close();
		logDone();
		log("=== Migration complete ===");
	}
	
	public static String attributeListToJSon(List<Attribute> list) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		Gson gson = new Gson();
		for (Attribute a : list) {
			if (forcedTypes.keySet().contains(a.getName())) {
				if (forcedTypes.get(a.getName()) == String.class) {
					map.put(a.getName(), a.getContent());
				} else if (forcedTypes.get(a.getName()) == Double.class) {
					map.put(a.getName(), Double.parseDouble(a.getContent()));
				}
			} else {
				String content = a.getContent();
				if (NumberUtils.isNumber(content)) {
					map.put(a.getName(), Double.parseDouble(content));
				} else {
					map.put(a.getName(), content);
				}
			}
		}
		return gson.toJson(map);
	}
	
	/*
	 * from SelectProcessInstanceModel.java
	 */
	public static List<ProcessInstanceDatabaseHandle> loadAllProcessInstances(Connection connection) {
		List<ProcessInstanceDatabaseHandle> allHandles = new ArrayList<ProcessInstanceDatabaseHandle>();
		try {
			PreparedStatement statement = connection.prepareStatement(
					"select i.database_id, i.id, i.data, p.id from process_instance i, process p where i.process = p.database_id "
							+ assembleIncludedProcesses(connection));
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				long databaseId = resultSet.getLong(1);
				String id = resultSet.getString(2);
				String attributes = resultSet.getString(3);
				String processId = resultSet.getString(4);

				ProcessInstanceDatabaseHandle handle = new ProcessInstanceDatabaseHandle(databaseId, id, attributes, processId);
				allHandles.add(handle);
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allHandles;
	}
	
	/*
	 * from SelectProcessInstanceModel.java
	 */
	public static String assembleIncludedProcesses(Connection connection) {
		StringBuilder query = new StringBuilder("and p.database_id");
		List<Process> toProcess = new ArrayList<Process>();

		query.append(" (");
		boolean first = true;

		for (Process current : toProcess) {
			if (!first) {
				query.append(", ");
			}

			try {
				query.append(DatabasePromReader.getProcessDatabaseId(current, connection));
			} catch (SQLException e) {
				if (!first) {
					query.replace(query.length() - 2, query.length(), "");
				}
			}

			first = false;
		}

		// no processes defined
		if (query.charAt(query.length() - 1) == '(') {
			return "";
		}

		query.append(")");
		return query.toString();
	}
	
	public static long databaseIdToSubjectId(long databaseid, Connection connection) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select fk_subject from process_instance where database_id = ?");
		statement.setLong(1, databaseid);
		ResultSet resultSet = statement.executeQuery();
		long subject_id = -1;
		while (resultSet.next()) {
			subject_id = resultSet.getLong(1);
		}
		statement.close();
		return subject_id;
	}
	
	public static long getIdOfLastTimePhase(Connection connection) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("SELECT pk_time_phase FROM time_phases WHERE pk_time_phase = LAST_INSERT_ID()");
		ResultSet resultSet = statement.executeQuery();
		resultSet.next();
		long time_phase_id = resultSet.getLong(1);
		statement.close();
		return time_phase_id;
	}
	
	private static void log(String message) {
		System.out.println(logDateFormat.format(new Date()) + " - " + message);
	}
	
	private static void logStart(String message) {
		System.out.print(logDateFormat.format(new Date()) + " - " + message + "... ");
	}
	
	private static void logDone() {
		System.out.println("Done! [" + logDateFormat.format(new Date()) + "]");
	}
}
