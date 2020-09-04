package oracleuserblock;

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
import java.sql.Timestamp;
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

import connection.OracleConnection;
import pluginmessages.CommonPluginMessages;
/**
 * @File_Desc: Oracle User Block Status Monitor Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :OracleUserBlock
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class OracleUserBlock extends BasePlugin {
	private String name;
	private int sleepInterval;

	private String header_message = "";
	private String footer_message = "";
	private String ConnectionString;
	private String result = "";
	private int scheduleType;
	private String thershold;
	String postData = "";
	String serverID = "";
	String status = "";
	String str_instanceName = "";
	private String metricInstanceId;

	@Override
	public void run() {
		try {

			CommonPluginMessages cpm = new CommonPluginMessages();

			serverID = cpm.serverId();

			WebResource webResource = cpm.getURL("Linux Oracle: Database TableSpace Size");

			while (true) {

				postData = getPostData();
				if (!postData.isEmpty()) {

					logger.info("OracleUserBlock DATA sent: " + postData);

					try {
						ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
								.type("application/json").post(ClientResponse.class, postData);
						logger.info("OracleUserBlock POST response: " + response);
					}

					catch (Exception ex) {

						logger.error("OracleUserBlock POST ERROR:" + ex.toString());
					}
				}
				int timeinterval = cpm.buildSchedule(scheduleType, sleepInterval);
				if (timeinterval != 0) {
					Thread.sleep(timeinterval);
				}
			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("OracleUserBlock threw error, full stack trace follows:" + e1);
			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("OracleUserBlock threw error, full stack trace follows:" + e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("OracleUserBlock threw error, full stack trace follows:" + e3);
			// e3.printStackTrace();
		} catch (XPathExpressionException e) {

			// e.printStackTrace();
		} catch (ParserConfigurationException e) {

			// e.printStackTrace();
		} catch (SAXException e) {

			// e.printStackTrace();
		} catch (Exception ex) {
			logger.error("OracleUserBlock Threw ERROR:" + ex.toString());

		}
	}

	public void updateData() throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException,
			XPathExpressionException, ParserConfigurationException, SAXException {
		String blocked_secs = "";
		String id = "";
		String info = "";
		String instanceName;
		String blocked_count = "";
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
						"WITH blocked_resources AS (select id1 ,id2 ,SUM(ctime) as blocked_secs ,COUNT(1) as blocked_count ,type from v$lock where request > 0 group by type,id1,id2 ) ,blockers AS (select L.id1, L.id2, L.type, L.sid ,BR.blocked_secs ,BR.blocked_count from v$lock L ,blocked_resources BR where BR.type = L.type and BR.id1 = L.id1 and BR.id2 = L.id2 and L.lmode > 0 and L.block <> 0 )select B.id1||'_'||B.id2||'_'||S.sid||'_'||S.serial# as id ,'SID,SERIAL:'||S.sid||','||S.serial#||',LOCK_TYPE:'||B.type||',PROGRAM:'||S.program||',MODULE:'||S.module||',ACTION:'||S.action||',MACHINE:'||S.machine||',OSUSER:'||S.osuser||',USERNAME:'||S.username as info ,B.blocked_secs ,B.blocked_count from v$session S ,blockers B where B.sid = S.sid");
				String ag = query.toString();
				rs = (ResultSet) ps.executeQuery(ag);
				result = "";
				while (rs.next()) {
					id = rs.getString("ID");

					info = rs.getString("INFO");
					blocked_secs = rs.getString("BLOCKED_SECS");
					blocked_count = rs.getString("BLOCKED_COUNT");
					instanceName = str_instanceName;

					result = result
							+ "  \\u003cOracleUserBlock  resultCode=\\\"0\\\" resultMessage=\\\"Oracle User Block returned: localhost\\\" ";

					result = result + " id=\\\"" + id + "\\\"";
					result = result + " info=\\\"" + info + "\\\"";
					result = result + " blocked_secs=\\\"" + blocked_secs + "\\\"";
					result = result + " blocked_count=\\\"" + blocked_count + "\\\"";
					result = result + " instance_name=\\\"" + instanceName + "\\\"";

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

	public String getPostData() throws XPathExpressionException, InvalidPropertiesFormatException, IOException,
			ParserConfigurationException, SAXException, ClassNotFoundException, SQLException {

		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ssssss'Z'");
		Date date = new Date();
		String formattedTime = dateformat.format(date);
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

		String newData = eElement.getAttribute("Data");

		String newInstanceName = "";
		String newInstanceNames = "";

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

		String str_product = eElement.getAttribute("product");
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
			pe = product_level;

		}
		updateData();
		if (status.contains("Error:")) {
			// to do check agent error status
			String errorPostData = cm.buildErrorString("OracleUserBlock", "PostData", status, serverID,
					str_metricInstanceId, formattedTime, str_product, IpAddress);

			if (!errorPostData.isEmpty()) {
				logger.info("AgentError DATA sent: " + errorPostData);

				OracleConnection oracleconn = new OracleConnection();

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

			postData = "{" + "\"" + "Data" + "\"" + ":" + "\"" + "\\" + "u003c" + "OracleUserBlockPluginOutput ";
			postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
			postData = postData + " product=\\\"" + pd.replaceAll("\t", " ") + "\\\"";
			postData = postData + " productVersion=\\\"" + str_Version.replaceAll("\t", " ") + "\\\"";
			postData = postData + " productLevel=\\\"" + pl.replaceAll("\t", " ") + "\\\"";
			postData = postData + " productEdition=\\\"" + pe.replaceAll("\t", " ") + "\\\"";
			postData = postData + " metricInstanceId=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " instanceName=\\\"" + str_instanceName + "\\\"";
			postData = postData + " label=\\\"" + str_label + "\\\"";
			postData = postData + "\\u003e\\r\\n";
			postData = postData + result;
			postData = postData + "\\u003c/OracleUserBlockPluginOutput\\u003e\",";
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

	public String getAgentLogMessage() {

		java.util.Date date = new java.util.Date();
		java.sql.Timestamp datetime = new Timestamp(date.getTime());

		String Message = "OracleUserBlock" + header_message + result + footer_message;
		;

		String text = Message;
		String newText = text.replace("\\", "");
		String newText1 = newText.replace("u0027", "");
		String newText2 = newText1.replace("u003e" + "\"" + ",", "");
		String newText3 = newText2.replace("/", "");

		String AgentLog_Message = "INFO" + " " + datetime + " " + "[6]:" + " " + "Posting" + " " + "<" + newText3 + " "
				+ "/" + ">" + "\n\n";

		return AgentLog_Message;

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
