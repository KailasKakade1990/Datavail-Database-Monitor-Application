package oracleasmdisksize;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
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
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import connection.MysqlConnection;
import connection.OracleConnection;
import pluginmessages.CommonPluginMessages;
/**
 * @File_Desc: Oracle ASM Disk Size Monitor  Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :OracleAsmDiskSize
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class OracleAsmDiskSize extends BasePlugin {
	private String name;
	private int sleepInterval;
	private String ConnectionString;
	private String result = "";
	private int scheduleType;
	private String thershold;
	private String disk_group;
	private String state;
	private String type;
	private String num_of_disk;
	private String mirrors;
	private String total_gb;
	private String free_gb;
	private String pct_used;
	private String usable_total_gb;
	private String usable_free_gb;
	private String usable_pct_used;
	private String reguired_for_mirror_gb;

	String postData = "";
	String serverID = "";
	String status = "";
	private String metricInstanceId;

	@Override
	public void run() {
		try {

			CommonPluginMessages cpm = new CommonPluginMessages();

			serverID = cpm.serverId();

			WebResource webResource = cpm.getURL("Linux Oracle: Database ASM Disk Size");
			while (true) {

				String postData = getPostData();
				if (!postData.isEmpty()) {
					logger.info("LinuxOracleDatabaseASMDiskSize DATA sent: " + postData);
					try {
						ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
								.type("application/json").post(ClientResponse.class, postData);
						logger.info("LinuxOracleDatabaseASMDiskSize POST response: " + response);
					} catch (Exception ex) {

						logger.error("LinuxOracleDatabaseASMDiskSize POST ERROR:"+ ex.toString());
					}
				}
				int timeinterval = cpm.buildSchedule(scheduleType, sleepInterval);
				if (timeinterval != 0) {
					Thread.sleep(timeinterval);
				}
			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("LinuxOracleDatabaseASMDiskSize threw error, full stack trace follows:"+ e1);
			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("LinuxOracleDatabaseASMDiskSize threw error, full stack trace follows:"+e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("LinuxOracleDatabaseASMDiskSize threw error, full stack trace follows:"+ e3);
			// e3.Trace();
		} catch (XPathExpressionException e) {

			// e.printStackTrace();
		} catch (ParserConfigurationException e) {

			// e.printStackTrace();
		} catch (SAXException e) {

		} catch (Exception ex) {
			logger.error("LinuxOracleDatabaseASMDiskSize Threw ERROR:"+ ex.toString());

		}

	}

	public void updateData(String instanceName)
			throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException,
			XPathExpressionException, ParserConfigurationException, SAXException {

		Connection conn = null;
		Statement ps = null;
		ResultSet rs = null;

		try {
			OracleConnection oracleconn = new OracleConnection();

			conn = oracleconn.beanConnection(ConnectionString);

			ps = (Statement) conn.createStatement();

			if (conn != null) {

				StringBuilder query = new StringBuilder();
				query.append(
						"SELECT * FROM(SELECT name disk_group, state, type, num_of_disk, redundancy_factor -1 mirrors, ROUND(total_gb, 2) total_gb, ROUND(free_gb, 2) free_gb, ROUND(100 - ((free_gb / redundancy_factor) / (total_gb / redundancy_factor) * 100)) pct_used, ");
				query.append(
						"ROUND((total_gb - required_gb) / redundancy_factor, 2) usable_total_gb, ROUND(usable_file_gb, 2) usable_free_gb, ROUND(100 - ((usable_file_gb / ((total_gb - required_gb) / redundancy_factor)) * 100)) usable_pct_used, ROUND(required_gb, 2) reguired_for_mirror_gb FROM(SELECT dg.group_number, dg.name, dg.state state, NVL (dg.total_mb, 0) / 1024 total_gb,NVL(dg.free_mb, 0) / 1024 free_gb,TYPE, NVL(dg.usable_file_mb, 0) / 1024 usable_file_gb,NVL(dg.required_mirror_free_mb, 0) / 1024 required_gb,NVL(usable_file_mb, 0) / 1024 usable_gb,DECODE(type, 'EXTERN', 1, 'NORMAL', 2, 'HIGH', 3, 1) redundancy_factor,dc.num_of_disk FROM v$asm_diskgroup dg,");
				query.append(
						"(SELECT group_number, COUNT(*) num_of_disk FROM v$asm_disk GROUP BY group_number) dc WHERE dg.group_number = dc.group_number))");

				String ag = query.toString();
				rs = (ResultSet) ps.executeQuery(ag);
				result = "";
				while (rs.next()) {

					disk_group = rs.getString("disk_group");
					state = rs.getString("state");
					type = rs.getString("type");
					num_of_disk = rs.getString("num_of_disk");
					mirrors = rs.getString("mirrors");
					total_gb = rs.getString("total_gb");
					free_gb = rs.getString("free_gb");
					pct_used = rs.getString("pct_used");
					usable_total_gb = rs.getString("usable_total_gb");
					usable_free_gb = rs.getString("usable_free_gb");
					usable_pct_used = rs.getString("usable_pct_used");
					reguired_for_mirror_gb = rs.getString("reguired_for_mirror_gb");

					result = result
							+ "  \\u003cDatabaseASMDiskSizeResult resultCode=\\\"0\\\" resultMessage=\\\"DatabaseASMDiskSizeResult: localhost\\\" ";
					result = result + " disk_group=\\\"" + disk_group + "\\\"";
					result = result + " state=\\\"" + state + "\\\"";
					result = result + " type=\\\"" + type + "\\\"";
					result = result + " num_of_disk=\\\"" + num_of_disk + "\\\"";
					result = result + " mirrors=\\\"" + mirrors + "\\\"";
					result = result + " total_gb=\\\"" + total_gb + "\\\"";
					result = result + " free_gb=\\\"" + free_gb + "\\\"";
					result = result + " pct_used=\\\"" + pct_used + "\\\"";
					result = result + " usable_total_gb=\\\"" + usable_total_gb + "\\\"";
					result = result + " usable_free_gb=\\\"" + usable_free_gb + "\\\"";
					result = result + " usable_pct_used=\\\"" + usable_pct_used + "\\\"";
					result = result + " reguired_for_mirror_gb=\\\"" + reguired_for_mirror_gb + "\\\"";
					result = result + "/\\u003e\\r\\n";

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
		} finally {
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

	public String getPostData() throws XPathExpressionException, InvalidPropertiesFormatException, IOException,
			ParserConfigurationException, SAXException, ClassNotFoundException, SQLException {

		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ssssss'Z'");
		Date date = new Date();
		String formattedTime = dateformat.format(date);
		@SuppressWarnings("unused")
		String datetimestamp = "timestamp=" + "\\" + "\"" + formattedTime + "\\" + "\"";

		File configfile = new File("config.xml");

		FileInputStream fileInput = new FileInputStream(configfile);

		Properties properties = new Properties();
		properties.loadFromXML(fileInput);

		String DeltaAgent_file = properties.getProperty("deltaagent");
		File fXmlFile = new File(DeltaAgent_file);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		String xPathString = "/AgentConfiguration/MetricInstance[@Id='" + metricInstanceId + "']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(xPathString);
		Node nNode = (Node) xPathExpression.evaluate(doc, XPathConstants.NODE);

		Element eElement = (Element) nNode;
		String str_metricInstanceId = eElement.getAttribute("Id");
		String str_label = eElement.getAttribute("Label");

		String str_product = eElement.getAttribute("product");
		String newData = eElement.getAttribute("Data");

		String newInstanceName = "";
		String newInstanceNames = "";
		String str_instanceName = "";
		int startindex = 0;
		int endindex = 0;

		startindex = newData.lastIndexOf("InstanceName");
		endindex = newData.lastIndexOf("InstanceId");
		String latest = newData.substring(startindex, endindex);
		latest.trim();
		newInstanceName = latest.replace("InstanceName=", "");
		newInstanceNames = newInstanceName.replaceAll("\"", "");
		newInstanceNames.trim();
		str_instanceName = newInstanceNames;

		CommonPluginMessages cm = new CommonPluginMessages();
		String hostname = cm.RunLinuxCommand("hostname");

		InetAddress addr = InetAddress.getLocalHost();
		String IpAddress = addr.getHostAddress();

		System.currentTimeMillis();

		OracleConnection orcconn = new OracleConnection();
		String product = orcconn.runOracleQuery(ConnectionString,
				"select banner from v$version where banner like 'Oracle%'");
		String pd = "";
		if (!product.contains("Error:") && product != "") {
			pd = product;
			pd = pd.trim().replaceAll("\\s{2,}", " ");

		}

		String str_Result = orcconn.runOracleQuery(ConnectionString,
				"select banner from v$version where banner like 'PL%'");
		String str_Version = "";
		if (!str_Result.contains("Error:") && str_Result != "") {
			str_Version = str_Result;

		}

		String product_level = orcconn.runOracleQuery(ConnectionString,
				"select banner from v$version where banner like 'CORE%'");

		product_level = String.format("%-20s", product_level);

		String pl = "";

		if (!product_level.contains("Error:") && product_level != "") {
			pl = product_level;

		}

		String product_edition = orcconn.runOracleQuery(ConnectionString,
				"select banner from v$version where banner like 'TNS%'");
		String pe = "";

		if (!product_edition.contains("Error:") && product_edition != "") {
			pe = product_edition;

		}

		updateData(str_instanceName);
		if (status.contains("Error:")) {
			// to do check agent error status
			String errorPostData = cm.buildErrorString("OracleAsmDiskSize", "PostData", status, serverID,
					str_metricInstanceId, formattedTime, str_product, IpAddress);

			if (!errorPostData.isEmpty()) {
				logger.info("AgentError DATA sent: " + errorPostData);

				MysqlConnection oracleconn = new MysqlConnection();

				WebResource webResource = oracleconn.client();

				try {
					ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
							.type("application/json").post(ClientResponse.class, errorPostData);
					logger.info("AgentError POST response: " + response);
				} catch (Exception ex) {
					logger.info("AgentError POST ERROR: " + ex.toString());

				}
			}

		} else {
			postData = "{" + "\"" + "Data" + "\"" + ":" + "\"" + "\\" + "u003c" + "DatabaseASMDiskSizePluginOutput ";
			postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
			postData = postData + " product=\\\"" + pd.replaceAll("\t", " ") + "\\\"";
			postData = postData + " productVersion=\\\"" + str_Version.replaceAll("\t", " ") + "\\\"";
			postData = postData + " productLevel=\\\"" + pl.replaceAll("\t", " ") + "\\\"";
			postData = postData + " productEdition=\\\"" + pe.replaceAll("\t", " ") + "\\\"";
			postData = postData + " metricInstanceId=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " label=\\\"" + str_label + "\\\"";
			postData = postData + " instanceName=\\\"" + str_instanceName + "\\\"";
			postData = postData + "\\u003e\\r\\n";
			postData = postData + result;
			postData = postData + "\\u003c/DatabaseASMDiskSizePluginOutput\\u003e\",";
			postData = postData + " \"Hostname\"" + ":\"" + hostname + "\"," + "\"IpAddress\"" + ":\"" + IpAddress
					+ "\",";
			postData = postData + " \"ServerId\"" + ":\"" + serverID + "\"," + "\"TenantId\"" + ":"
					+ "\"1a19a18a-846c-49da-93c1-8948afdc0151\"";
			postData = postData + "," + "\"Timestamp\"" + ":\"" + "\\/Date(" + System.currentTimeMillis() + ")\\/"
					+ "\"";
			postData = postData + "," + "\"Id\":null" + "," + "\"PopReceipt\":0" + "," + "\"DequeueCount\":0}";
		}
		return postData;

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
	 * @return the metricInstanceId
	 */
	public String getMetricInstanceId() {
		return metricInstanceId;
	}

	/**
	 * @param metricInstanceId
	 *            the metricInstanceId to set
	 */
	public void setMetricInstanceId(String metricInstanceId) {
		this.metricInstanceId = metricInstanceId;
	}

}
