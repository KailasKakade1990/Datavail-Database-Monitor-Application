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
public class LinuxHCTableSpaceSizePlugin extends BasePlugin {
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

	public LinuxHCTableSpaceSizePlugin() {
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
			postData = postData + " MonitorName=\\\"" + "LinuxHCTableSpaceSizePlugin" + "\\\"";
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
		String User = "";

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
						"SELECT tablespace_name as Tablespace,ROUND ( (alloc_bytes - free_bytes) / alloc_bytes * 100)  as Alloc_Usage,ROUND (alloc_bytes / 1024 / 1024) as Alloc_Mb,ROUND (free_bytes / 1024 / 1024) as Alloc_Free_Mb, ROUND ( (alloc_bytes - free_bytes) / 1024 / 1024)  as Alloc_Used_Mb, ROUND (free_bytes / alloc_bytes * 100) as Alloc_Free_Pct,ROUND (DECODE(max_bytes,0,alloc_bytes, max_bytes) / 1024 / 1024) as Max_Extendable_Mb,ROUND ( (alloc_bytes - free_bytes) / DECODE(max_bytes, 0, alloc_bytes, max_bytes) * 100) as Extendable_Usage FROM (SELECT a.tablespace_name tablespace_name, a.bytes_alloc alloc_bytes, NVL (b.bytes_free, 0) free_bytes, max_bytes max_bytes FROM (  SELECT f.tablespace_name, SUM (f.bytes) bytes_alloc, SUM ( DECODE (f.autoextensible, 'YES', f.maxbytes, 'NO', f.bytes)) max_bytes FROM dba_data_files f GROUP BY tablespace_name) a, (  SELECT f.tablespace_name, SUM (f.bytes) bytes_free FROM dba_free_space f  GROUP BY tablespace_name) b  WHERE a.tablespace_name = b.tablespace_name(+) UNION ALL SELECT h.tablespace_name tablespace_name,   SUM (h.bytes_free + h.bytes_used) alloc_bytes,  SUM ( (h.bytes_free + h.bytes_used) - NVL (p.bytes_used, 0))  free_bytes, SUM (f.maxbytes) max_bytes  FROM sys.v_$temp_space_header h,  sys.v_$temp_extent_pool p, dba_temp_files f WHERE p.file_id(+) = h.file_id AND p.tablespace_name(+) = h.tablespace_name AND f.file_id = h.file_id AND f.tablespace_name = h.tablespace_name  GROUP BY h.tablespace_name)ORDER BY 8 DESC");

				String ag = query.toString();
				rs = (ResultSet) ps.executeQuery(ag);

				int row = 1;
				while (rs.next()) {

					// Tablespace
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "Tablespace" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("Tablespace") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// Alloc_Usage
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "Alloc_Usage" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("Alloc_Usage") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// Alloc_Mb
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "Alloc_Mb" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("Alloc_Mb") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// Alloc_Free_Mb
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "Alloc_Free_Mb" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("Alloc_Free_Mb") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// Alloc_Used_Mb
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "Alloc_Used_Mb" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("Alloc_Used_Mb") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// Alloc_Free_Pct
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "Alloc_Free_Pct" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("Alloc_Free_Pct") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// Max_Extendable_Mb
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "Max_Extendable_Mb" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("Max_Extendable_Mb") + "\\\"";
					result = result + " /\\u003e";
					result = result + "\\u003c/Properties\\u003e";

					// Extendable_Usage
					result = result + "\\u003cProperties\\u003e";
					result = result + "\\u003cProperty";
					result = result + " Type=\\\"" + "Text" + "\\\"";
					result = result + " Row=\\\"" + row + "\\\"";
					result = result + " Key=\\\"" + "Extendable_Usage" + "\\\"";
					result = result + " Value=\\\"" + rs.getString("Extendable_Usage") + "\\\"";
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
