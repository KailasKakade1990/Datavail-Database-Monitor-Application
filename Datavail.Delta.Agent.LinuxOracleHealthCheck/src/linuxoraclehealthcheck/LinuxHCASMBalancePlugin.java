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
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 * 
 */
public class LinuxHCASMBalancePlugin extends BasePlugin {
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

	public LinuxHCASMBalancePlugin() {
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
			postData = postData + " MonitorName=\\\"" + "LinuxHCASMBalancePlugin" + "\\\"";
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
						"SELECT g.name as DiskGroup,ROUND (100* (  MAX ( (d.total_mb - d.free_mb) / d.total_mb)- MIN ( (d.total_mb - d.free_mb) / d.total_mb))/ MAX ( (d.total_mb - d.free_mb) / d.total_mb), 2) as Imbalance, ROUND(100 * (MAX (d.total_mb) - MIN (d.total_mb)) / MAX (d.total_mb),2) as Variance,ROUND (100 * (MIN (d.free_mb / d.total_mb)), 2) as MinFree,COUNT (*) as DiskCnt,g.TYPE as Type FROM v$asm_disk d, v$asm_diskgroup g WHERE d.group_number = g.group_number AND d.group_number <> 0 AND d.state = 'NORMAL' AND d.mount_status = 'CACHED' GROUP BY g.name, g.TYPE");

				String ag = query.toString();
				rs = (ResultSet) ps.executeQuery(ag);
				int row = 1;
				while (rs.next()) {
					// Disk Group
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "Disk Group" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("DiskGroup") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// Imbalance
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "Imbalance" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("Imbalance") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// Variance
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "Variance" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("Variance") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// MinFree
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "MinFree" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("MinFree") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// DiskCnt
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "DiskCnt" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("DiskCnt") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// Type
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "Type" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("Type") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					row++;
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
