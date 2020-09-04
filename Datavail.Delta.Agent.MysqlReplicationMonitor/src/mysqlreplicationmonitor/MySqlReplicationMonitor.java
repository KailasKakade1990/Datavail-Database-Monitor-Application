package mysqlreplicationmonitor;

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
import pluginmessages.CommonPluginMessages;
/**
 * @File_Desc: Mysql Replication Monitor  Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :MySqlReplicationMonitor
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class MySqlReplicationMonitor extends BasePlugin {
	private String name;
	private int sleepInterval;
	private String ConnectionString;
	private String resultStatus = "";
	int resultCode = 0;
	private String slave_io_running = "";
	private String slave_sql_running = "";
	private String seconds_behind_master = "";
	private int scheduleType;
	private String thershold;
	String postData = "";
	String serverID = "";
	String status = "";

	private String metricInstanceId;

	@Override
	public void run() {
		try {

			CommonPluginMessages CPM = new CommonPluginMessages();

			serverID = CPM.serverId();

			WebResource webResource = CPM.getURL("Linux MySQL: Replication Monitor Status");
			while (true) {

				String postData = getPostData();
				if (!postData.isEmpty()) {
					logger.info("MysqlReplicationMonitor DATA sent: " + postData);

					try {
						ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
								.type("application/json").post(ClientResponse.class, postData);
						logger.info("MysqlReplicationMonitor POST response: " + response);
					} catch (Exception ex) {

						logger.error(" MysqlReplicationMonitor POST ERROR:"+ ex.toString());
					}
				}
				int timeinterval = CPM.buildSchedule(scheduleType, sleepInterval);

				if (timeinterval != 0) {
					Thread.sleep(timeinterval);
				}
			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("MysqlReplicationMonitor threw error, full stack trace follows:"+ e1);
			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("MysqlReplicationMonitor threw error, full stack trace follows:"+ e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("MysqlReplicationMonitor threw error, full stack trace follows:"+ e3);
			// e3.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (Exception ex) {
			logger.error("MysqlReplicationMonitor Threw ERROR:"+ ex.toString());

		}

	}

	public void updateData()
			throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException {

		MysqlConnection mysqlconn = new MysqlConnection();
		@SuppressWarnings("unused")
		String queryResult = "";
		Connection conn = null;
		Statement ps = null;
		ResultSet rs = null;

		try {
			conn = mysqlconn.beanConnection(ConnectionString);
			ps = (Statement) conn.createStatement();

			rs = (ResultSet) ps.executeQuery("show slave status");

			while (rs.next()) {

				slave_io_running = rs.getString("Slave_IO_Running");
				slave_sql_running = rs.getString("Slave_SQL_Running");

				seconds_behind_master = rs.getString("Seconds_Behind_Master");
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

	public String getPostData() throws IOException, ParserConfigurationException, SAXException,
			XPathExpressionException, ClassNotFoundException, SQLException {

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
		MysqlConnection mysqlconn = new MysqlConnection();
		String str_Result = mysqlconn.runMysqlQuery(ConnectionString, "select version()");
		String str_Version = "";
		if (str_Result.contains("Error:") && str_Result != "") {
			str_Version = str_Result;
		}
		str_Result = mysqlconn.runMysqlQueryReturnColumn(ConnectionString, "SHOW VARIABLES LIKE '%version_comment%'",
				2);
		String str_Edition = "";
		if (!str_Result.contains("Error:") && str_Result != "") {
			str_Edition = str_Result;
		}

		updateData();
		if (status.contains("Error:")) {
			// to do check agent error status
			String errorPostData = cm.buildErrorString("MysqlReplicationMonitor", "PostData", status, serverID,
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

			str_product = "MySQL";
			postData = "{\"Data\"" + ":" + "\"\\u003cReplicationMonitorPlugin ";
			postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
			postData = postData + " product=\\\"" + str_product + "\\\"";
			postData = postData + " productVersion=\\\"" + str_Version + "\\\"";
			postData = postData + " productLevel=\\\"\\\"";
			postData = postData + " productEdition=\\\"" + str_Edition + "\\\"";
			postData = postData + " metricInstanceId=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " label=\\\"" + str_label + "\\\"";

			postData = postData + " Slave_IO_Running=\\\"" + slave_io_running + "\\\"";
			postData = postData + " Slave_SQL_Running=\\\"" + slave_sql_running + "\\\"";
			postData = postData + " Seconds_Behind_Master=\\\"" + seconds_behind_master + "\\\"";
			postData = postData + " resultMessage=\\\"Replication Status is not returned: " + str_instanceName + "\\\"";

			postData = postData + " instanceName=\\\"" + str_instanceName + "\\\"";
			postData = postData + " /\\u003e\"" + ",";
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

	public String getConnectionString() {
		return ConnectionString;
	}

	public void setConnectionString(String connectionString) {
		ConnectionString = connectionString;
	}

	public int getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(int scheduleType) {
		this.scheduleType = scheduleType;
	}

	public String getResultStatus() {
		return resultStatus;
	}

	public void setResultStatus(String resultStatus) {
		this.resultStatus = resultStatus;
	}

	public String getThershold() {
		return thershold;
	}

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
