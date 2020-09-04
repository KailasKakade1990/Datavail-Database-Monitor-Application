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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class LinuxHCDBAJobsPlugin extends BasePlugin {
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

	public LinuxHCDBAJobsPlugin() {
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
			postData = postData + " MonitorName=\\\"" + "LinuxHCDBAJobsPlugin" + "\\\"";
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

				query.append("select JOB,LOG_USER,PRIV_USER,SCHEMA_USER,LAST_DATE,LAST_SEC,THIS_DATE,THIS_SEC,NEXT_DATE,NEXT_SEC,TOTAL_TIME,BROKEN,INTERVAL,FAILURES,WHAT,NLS_ENV,MISC_ENV,INSTANCE from dba_jobs");

				String ag = query.toString();
				rs = (ResultSet) ps.executeQuery(ag);
				int row = 1;
				while (rs.next()) {
					
					// JOB
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "JOB" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("JOB") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// LOG_USER
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "LOG_USER" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("LOG_USER") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// PRIV_USER
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "PRIV_USER" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("PRIV_USER") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// SCHEMA_USER
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "SCHEMA_USER" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("SCHEMA_USER") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// LAST_DATE					
					String LAST_DATE = rs.getString("LAST_DATE");
					LocalDateTime datetime = LocalDateTime.parse(LAST_DATE, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
					String lastdate = datetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
					//System.out.println("newstring:------------------------------------------------------"+newstring); // 2011-01-18
					
					
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "LAST_DATE" + "\\\"";
					result = result + " Value=\\\"" +lastdate+ "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// LAST_SEC
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "LAST_SEC" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("LAST_SEC") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// THIS_DATE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "THIS_DATE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("THIS_DATE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// THIS_SEC
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "THIS_SEC" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("THIS_SEC") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// NEXT_DATE
					String NEXT_DATE = rs.getString("NEXT_DATE");
					LocalDateTime NEXT_DATEdatetime = LocalDateTime.parse(NEXT_DATE, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
					String nextdate = NEXT_DATEdatetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "NEXT_DATE" + "\\\"";
					result = result + " Value=\\\"" + nextdate + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// NEXT_SEC
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "NEXT_SEC" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("NEXT_SEC") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// TOTAL_TIME
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "TOTAL_TIME" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("TOTAL_TIME") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// BROKEN
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "BROKEN" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("BROKEN") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// INTERVAL
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "INTERVAL" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("INTERVAL") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// FAILURES
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "FAILURES" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("FAILURES") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// WHAT
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "WHAT" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("WHAT") + "\\\"";
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
					
					// MISC_ENV
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "MISC_ENV" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("MISC_ENV") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";
					
					// INSTANCE
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "INSTANCE" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("INSTANCE") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					row++;
				}

			}

		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			//e.printStackTrace(pw);
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
