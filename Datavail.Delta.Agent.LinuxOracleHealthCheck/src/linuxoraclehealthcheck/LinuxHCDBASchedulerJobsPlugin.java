package linuxoraclehealthcheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.datavail.plugins.BasePlugin;

import connection.OracleConnection;
/**
 * @PluginDesc:Oracle Health Check On Linux
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :Linux <> Plugin
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 */
public class LinuxHCDBASchedulerJobsPlugin extends BasePlugin {
	private String name;
	private int sleepInterval;
	private int scheduleType;
	private String ConnectionString;
	private String resultStatus = "";
	private String thershold;
	private String result = "";
	private String status = "";
	String instanceName = "";
	String instanceId = "";
	public int excuteLevel;

	public LinuxHCDBASchedulerJobsPlugin() {
		excuteLevel = 1;
	}

	@Override
	public void run() {

	}

	public String makedata(String data) throws IOException, ParserConfigurationException, SAXException,
			XPathExpressionException, ClassNotFoundException, SQLException {

		String[] strArr = data.split(",");

		String connString = strArr[0].replace("[connString=", "").trim();
		connString = connString.trim();
		instanceName = strArr[1].replaceAll("instanceName=", "").trim();
		instanceId = strArr[2].replaceAll("instanceId=", "").trim();
		instanceId = instanceId.replaceAll("]", "");
		String str = getPostData(connString);

		return (str);

	}

	public String getPostData(String connString) throws IOException, ParserConfigurationException, SAXException,
			XPathExpressionException, ClassNotFoundException, SQLException {
		String postData = "";
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ssssss'Z'");
		Date date = new Date();

		String formattedTime = dateformat.format(date);

		File configfile = new File("config.xml");

		FileInputStream fileInput = new FileInputStream(configfile);

		Properties properties = new Properties();
		properties.loadFromXML(fileInput);

		String DeltaAgent_file = properties.getProperty("deltaagent");
		File fXmlFile = new File(DeltaAgent_file);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		String xPathString = "/AgentConfiguration/MetricInstance[@AdapterClass='LinuxOracleHealthCheck']";

		XPath xPath = XPathFactory.newInstance().newXPath();

		XPathExpression xPathExpression = xPath.compile(xPathString);
		Node nNode = (Node) xPathExpression.evaluate(doc, XPathConstants.NODE);

		Element eElement = (Element) nNode;

		String str_metricInstanceId = eElement.getAttribute("Id");

		System.currentTimeMillis();
		updateData(connString);
		postData = "";

		if (result != "") {

			postData = "\\u003cMonitor ";

			postData = postData + " DBInstanceID=\\\"" + instanceId + "\\\"";
			postData = postData + " DatabaseID=\\\"" + "" + "\\\"";
			postData = postData + " AgentVersion=\\\"" + "6.0.0.0" + "\\\"";
			postData = postData + " DateTime=\\\"" + formattedTime + "\\\"";
			postData = postData + " MonitorInstanceID=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " MonitorName=\\\"" + "LinuxHCDBASchedulerJobsPlugin" + "\\\"";
			postData = postData + " TimeZone=\\\"" + "India Standard Time" + "\\\"";
			postData = postData + " Culture=\\\"" + "en-IN" + "\\\"";

			postData = postData + "\\u003e";

			postData = postData + result;

			postData = postData + "\\u003c/Monitor";
			postData = postData + "\\u003e";
		}

		return postData;
	}

	public void updateData(String ConnString)
			throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException,
			XPathExpressionException, ParserConfigurationException, SAXException {
		String dbSizeInGB = "";

		Connection conn = null;
		Statement ps = null;
		ResultSet rs = null;
		try {
			OracleConnection oracleconn = new OracleConnection();

			conn = oracleconn.beanConnection(ConnString);

			ps = (Statement) conn.createStatement();

			if (conn != null) {

				StringBuilder query = new StringBuilder();

				query.append(
						"select OWNER, JOB_NAME, JOB_SUBNAME, JOB_STYLE, JOB_CREATOR, CLIENT_ID, GLOBAL_UID, PROGRAM_OWNER, PROGRAM_NAME, JOB_TYPE, JOB_ACTION, NUMBER_OF_ARGUMENTS, SCHEDULE_OWNER, SCHEDULE_NAME, SCHEDULE_TYPE, START_DATE, REPEAT_INTERVAL, EVENT_QUEUE_OWNER, EVENT_QUEUE_NAME, EVENT_QUEUE_AGENT, EVENT_CONDITION, EVENT_RULE, FILE_WATCHER_OWNER, FILE_WATCHER_NAME, END_DATE, JOB_CLASS, ENABLED, AUTO_DROP, RESTARTABLE, STATE, JOB_PRIORITY, RUN_COUNT, MAX_RUNS, FAILURE_COUNT, MAX_FAILURES, RETRY_COUNT, LAST_START_DATE, to_char(LAST_RUN_DURATION) LAST_RUN_DURATION, NEXT_RUN_DATE, to_char(SCHEDULE_LIMIT) SCHEDULE_LIMIT, to_char(MAX_RUN_DURATION) MAX_RUN_DURATION,LOGGING_LEVEL, STOP_ON_WINDOW_CLOSE, INSTANCE_STICKINESS, RAISE_EVENTS, SYSTEM, JOB_WEIGHT, NLS_ENV, SOURCE, NUMBER_OF_DESTINATIONS, DESTINATION_OWNER, DESTINATION, CREDENTIAL_OWNER, CREDENTIAL_NAME, INSTANCE_ID, COMMENTS, ALLOW_RUNS_IN_RESTRICTED_MODE, DEFERRED_DROP, FLAGS   from dba_scheduler_jobs");

				String ag = query.toString();
				rs = (ResultSet) ps.executeQuery(ag);
				int row = 1;
				while (rs.next()) {
					// OWNER
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "OWNER" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("OWNER") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// JOB_NAME
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "JOB_NAME" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("JOB_NAME") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// JOB_SUBNAME
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "JOB_SUBNAME" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("JOB_SUBNAME") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// JOB_SUBNAME
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "JOB_STYLE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("JOB_STYLE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// JOB_CREATOR
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "JOB_CREATOR" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("JOB_CREATOR") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// CLIENT_ID
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "CLIENT_ID" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("CLIENT_ID") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// GLOBAL_UID
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "GLOBAL_UID" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("GLOBAL_UID") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// PROGRAM_OWNER
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "PROGRAM_OWNER" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("PROGRAM_OWNER") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// PROGRAM_NAME
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "PROGRAM_NAME" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("PROGRAM_NAME") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// JOB_TYPE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "JOB_TYPE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("JOB_TYPE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// JOB_ACTION
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "JOB_ACTION" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("JOB_ACTION") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// NUMBER_OF_ARGUMENTS
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "NUMBER_OF_ARGUMENTS" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("NUMBER_OF_ARGUMENTS") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// SCHEDULE_OWNER
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "SCHEDULE_OWNER" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("SCHEDULE_OWNER") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// SCHEDULE_NAME
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "SCHEDULE_NAME" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("SCHEDULE_NAME") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// SCHEDULE_TYPE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "SCHEDULE_TYPE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("SCHEDULE_TYPE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// START_DATE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "START_DATE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("START_DATE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// REPEAT_INTERVAL
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "REPEAT_INTERVAL" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("REPEAT_INTERVAL") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// EVENT_QUEUE_OWNER
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "EVENT_QUEUE_OWNER" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("EVENT_QUEUE_OWNER") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// EVENT_QUEUE_NAME
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "EVENT_QUEUE_NAME" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("EVENT_QUEUE_NAME") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// EVENT_QUEUE_AGENT
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "EVENT_QUEUE_AGENT" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("EVENT_QUEUE_AGENT") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// EVENT_CONDITION
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "EVENT_CONDITION" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("EVENT_CONDITION") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// EVENT_RULE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "EVENT_RULE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("EVENT_RULE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// FILE_WATCHER_OWNER
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "FILE_WATCHER_OWNER" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("FILE_WATCHER_OWNER") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// FILE_WATCHER_NAME
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "FILE_WATCHER_NAME" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("FILE_WATCHER_NAME") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// END_DATE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "END_DATE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("END_DATE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// STATE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "STATE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("STATE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// JOB_CLASS
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "JOB_CLASS" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("JOB_CLASS") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// ENABLED
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "ENABLED" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("ENABLED") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// AUTO_DROP
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "AUTO_DROP" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("AUTO_DROP") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// RESTARTABLE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "RESTARTABLE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("RESTARTABLE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// JOB_PRIORITY
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "JOB_PRIORITY" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("JOB_PRIORITY") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// RUN_COUNT
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "RUN_COUNT" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("RUN_COUNT") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// MAX_RUNS
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "MAX_RUNS" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("MAX_RUNS") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// FAILURE_COUNT
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "FAILURE_COUNT" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("FAILURE_COUNT") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// MAX_FAILURES
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "MAX_FAILURES" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("MAX_FAILURES") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// RETRY_COUNT
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "RETRY_COUNT" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("RETRY_COUNT") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// LAST_START_DATE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "LAST_START_DATE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("LAST_START_DATE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// LAST_RUN_DURATION
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "LAST_RUN_DURATION" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("LAST_RUN_DURATION") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// NEXT_RUN_DATE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "NEXT_RUN_DATE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("NEXT_RUN_DATE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// SCHEDULE_LIMIT
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "SCHEDULE_LIMIT" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("SCHEDULE_LIMIT") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// MAX_RUN_DURATION
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "MAX_RUN_DURATION" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("MAX_RUN_DURATION") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// LOGGING_LEVEL
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "LOGGING_LEVEL" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("LOGGING_LEVEL") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// STOP_ON_WINDOW_CLOSE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "STOP_ON_WINDOW_CLOSE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("STOP_ON_WINDOW_CLOSE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// INSTANCE_STICKINESS
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "INSTANCE_STICKINESS" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("INSTANCE_STICKINESS") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// RAISE_EVENTS
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "RAISE_EVENTS" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("RAISE_EVENTS") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// SYSTEM
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "SYSTEM" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("SYSTEM") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// JOB_WEIGHT
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "JOB_WEIGHT" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("JOB_WEIGHT") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// NLS_ENV
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "NLS_ENV" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("NLS_ENV") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// SOURCE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "SOURCE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("SOURCE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// NUMBER_OF_DESTINATIONS
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "NUMBER_OF_DESTINATIONS" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("NUMBER_OF_DESTINATIONS") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// DESTINATION_OWNER
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "DESTINATION_OWNER" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("DESTINATION_OWNER") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// DESTINATION
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "DESTINATION" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("DESTINATION") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// CREDENTIAL_OWNER
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "CREDENTIAL_OWNER" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("CREDENTIAL_OWNER") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// CREDENTIAL_NAME
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "CREDENTIAL_NAME" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("CREDENTIAL_NAME") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// INSTANCE_ID
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "INSTANCE_ID" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("INSTANCE_ID") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// COMMENTS
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "COMMENTS" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("COMMENTS") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// ALLOW_RUNS_IN_RESTRICTED_MODE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "ALLOW_RUNS_IN_RESTRICTED_MODE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("ALLOW_RUNS_IN_RESTRICTED_MODE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// DEFERRED_DROP
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "DEFERRED_DROP" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("DEFERRED_DROP") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// FLAGS
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "FLAGS" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("FLAGS") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					row++;
				}

			}

		} catch (Exception e) {

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			int lenoferrorstr = stackTrace.length();
			if (lenoferrorstr > 200) {
				status = "Error:" + stackTrace.substring(0, 200);
			} else {
				status = "Error:" + stackTrace.substring(0, lenoferrorstr - 1);

			}
		}

		finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					/* ignored */}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					/* ignored */}
			}
			if (conn != null) {

				try {
					conn.close();
				} catch (SQLException e) {
					/* ignored */}
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSleepInterval() {
		return sleepInterval;
	}

	public void setSleepInterval(int sleepInterval) {
		this.sleepInterval = sleepInterval;
	}

	/**
	 * @return the connectionString
	 */
	public String getConnectionString() {
		return ConnectionString;
	}

	/**
	 * @param connectionString
	 *            the connectionString to set
	 */
	public void setConnectionString(String connectionString) {
		ConnectionString = connectionString;
	}

	/**
	 * @return the scheduleType
	 */
	public int getScheduleType() {
		return scheduleType;
	}

	/**
	 * @param scheduleType
	 *            the scheduleType to set
	 */
	public void setScheduleType(int scheduleType) {
		this.scheduleType = scheduleType;
	}

	/**
	 * @return the thershold
	 */
	public String getThershold() {
		return thershold;
	}

	/**
	 * @param thershold
	 *            the thershold to set
	 */
	public void setThershold(String thershold) {
		this.thershold = thershold;
	}

	/**
	 * @return the resultStatus
	 */
	public String getResultStatus() {
		return resultStatus;
	}

	/**
	 * @param resultStatus
	 *            the resultStatus to set
	 */
	public void setResultStatus(String resultStatus) {
		this.resultStatus = resultStatus;
	}

	/**
	 * @return the excuteLevel
	 */
	public int getExcuteLevel() {
		return excuteLevel;
	}

	/**
	 * @param excuteLevel
	 *            the excuteLevel to set
	 */
	public void setExcuteLevels(int excuteLevel) {
		this.excuteLevel = excuteLevel;
	}
}
