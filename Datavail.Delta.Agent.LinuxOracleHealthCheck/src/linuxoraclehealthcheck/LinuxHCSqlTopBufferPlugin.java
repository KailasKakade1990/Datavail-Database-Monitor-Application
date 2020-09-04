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
public class LinuxHCSqlTopBufferPlugin extends BasePlugin {
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

	public LinuxHCSqlTopBufferPlugin() {
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
			postData = postData + " MonitorName=\\\"" + "LinuxHCSqlTopBufferPlugin" + "\\\"";
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
						"SELECT *  FROM (  SELECT sql_id  as SQL_ID,last_load_time as Last_Load,last_active_time as Last_Active, sorts as Sorts,loaded_versions as Load_Version,open_versions as ");

				query.append(
						" Open_Version,users_opening as User_Opening ,fetches as Fetches,executions as Execs,disk_reads as Disk_Read,buffer_gets as Buffer_Gets FROM v$sql ORDER BY buffer_gets DESC) a WHERE ROWNUM < 11");
				String ag = query.toString();
				rs = (ResultSet) ps.executeQuery(ag);
				int row = 1;
				try {
					while (rs.next()) {

						// SQL_ID
						result = result + "\\u003cProperties\\u003e";
						result = result + "\\u003cProperty";
						result = result + " Type=\\\"" + "Text" + "\\\"";
						result = result + " Row=\\\"" + row + "\\\"";
						result = result + " Key=\\\"" + "SQL ID" + "\\\"";
						result = result + " Value=\\\"" + rs.getString("SQL_ID") + "\\\"";
						result = result + " /\\u003e";
						result = result + "\\u003c/Properties\\u003e";

						// LAST_LOAD
						result = result + "\\u003cProperties\\u003e";
						result = result + "\\u003cProperty";
						result = result + " Type=\\\"" + "Text" + "\\\"";
						result = result + " Row=\\\"" + row + "\\\"";
						result = result + " Key=\\\"" + "Last Load" + "\\\"";
						result = result + " Value=\\\"" + rs.getString("Last_Load") + "\\\"";
						result = result + " /\\u003e";
						result = result + "\\u003c/Properties\\u003e";

						// LAST_ACTIVE
						result = result + "\\u003cProperties\\u003e";
						result = result + "\\u003cProperty";
						result = result + " Type=\\\"" + "Text" + "\\\"";
						result = result + " Row=\\\"" + row + "\\\"";
						result = result + " Key=\\\"" + "Last Active" + "\\\"";
						result = result + " Value=\\\"" + rs.getString("Last_Active") + "\\\"";
						result = result + " /\\u003e";
						result = result + "\\u003c/Properties\\u003e";

						// SORTS
						result = result + "\\u003cProperties\\u003e";
						result = result + "\\u003cProperty";
						result = result + " Type=\\\"" + "Text" + "\\\"";
						result = result + " Row=\\\"" + row + "\\\"";
						result = result + " Key=\\\"" + "Sorts" + "\\\"";
						result = result + " Value=\\\"" + rs.getString("Sorts") + "\\\"";
						result = result + " /\\u003e";
						result = result + "\\u003c/Properties\\u003e";

						// LOAD_VERSION
						result = result + "\\u003cProperties\\u003e";
						result = result + "\\u003cProperty";
						result = result + " Type=\\\"" + "Text" + "\\\"";
						result = result + " Row=\\\"" + row + "\\\"";
						result = result + " Key=\\\"" + "Load Version" + "\\\"";
						result = result + " Value=\\\"" + rs.getString("Load_Version") + "\\\"";
						result = result + " /\\u003e";
						result = result + "\\u003c/Properties\\u003e";

						// OPEN_VERSION
						result = result + "\\u003cProperties\\u003e";
						result = result + "\\u003cProperty";
						result = result + " Type=\\\"" + "Text" + "\\\"";
						result = result + " Row=\\\"" + row + "\\\"";
						result = result + " Key=\\\"" + "Open Version" + "\\\"";
						result = result + " Value=\\\"" + rs.getString("Open_Version") + "\\\"";
						result = result + " /\\u003e";
						result = result + "\\u003c/Properties\\u003e";

						// USER_OPENING
						result = result + "\\u003cProperties\\u003e";
						result = result + "\\u003cProperty";
						result = result + " Type=\\\"" + "Text" + "\\\"";
						result = result + " Row=\\\"" + row + "\\\"";
						result = result + " Key=\\\"" + "User Opening" + "\\\"";
						result = result + " Value=\\\"" + rs.getString("User_Opening") + "\\\"";
						result = result + " /\\u003e";
						result = result + "\\u003c/Properties\\u003e";

						// FETCHES
						result = result + "\\u003cProperties\\u003e";
						result = result + "\\u003cProperty";
						result = result + " Type=\\\"" + "Text" + "\\\"";
						result = result + " Row=\\\"" + row + "\\\"";
						result = result + " Key=\\\"" + "Fetches" + "\\\"";
						result = result + " Value=\\\"" + rs.getString("Fetches") + "\\\"";
						result = result + " /\\u003e";
						result = result + "\\u003c/Properties\\u003e";

						// EXECS
						result = result + "\\u003cProperties\\u003e";
						result = result + "\\u003cProperty";
						result = result + " Type=\\\"" + "Text" + "\\\"";
						result = result + " Row=\\\"" + row + "\\\"";
						result = result + " Key=\\\"" + "Execs" + "\\\"";
						result = result + " Value=\\\"" + rs.getString("Execs") + "\\\"";
						result = result + " /\\u003e";
						result = result + "\\u003c/Properties\\u003e";

						// DISK_READ
						result = result + "\\u003cProperties\\u003e";
						result = result + "\\u003cProperty";
						result = result + " Type=\\\"" + "Text" + "\\\"";
						result = result + " Row=\\\"" + row + "\\\"";
						result = result + " Key=\\\"" + "Disk Read" + "\\\"";
						result = result + " Value=\\\"" + rs.getString("Disk_Read") + "\\\"";
						result = result + " /\\u003e";
						result = result + "\\u003c/Properties\\u003e";

						// BUFFER_GETS
						result = result + "\\u003cProperties\\u003e";
						result = result + "\\u003cProperty";
						result = result + " Type=\\\"" + "Text" + "\\\"";
						result = result + " Row=\\\"" + row + "\\\"";
						result = result + " Key=\\\"" + "Buffer Gets" + "\\\"";
						result = result + " Value=\\\"" + rs.getString("Buffer_Gets") + "\\\"";
						result = result + " /\\u003e";
						result = result + "\\u003c/Properties\\u003e";

						row++;
					}
				} catch (Exception ex) {

					// System.out.println("Exception" + ex.toString());

				}
			}

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			// e.printStackTrace(pw);
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
