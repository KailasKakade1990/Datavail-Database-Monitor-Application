package mysqlconnectionmonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
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
 * @File_Desc:Mysql Connection Monitor info Plugin  Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :MysqlConnectionMonitor
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class MysqlConnectionMonitor extends BasePlugin {

	private String name;
	private int sleepInterval;
	private int scheduleType;
	private String ConnectionString;
	private String resultStatus = "";
	private int resultCode = 0;
	private String thershold;
	private String queryResult = "";
	String postData = "";
	String serverID = "";
	String status = "";
	private String metricInstanceId;

	@Override
	public void run() {

		try {

			CommonPluginMessages cpm = new CommonPluginMessages();

			serverID = cpm.serverId();

			WebResource webResource = cpm.getURL("Linux MySQL: Database Connection monitor");

			while (true) {
				updateData();

				postData = getPostData();
				if (!postData.isEmpty()) {

					logger.info("MysqlConnectionMonitor DATA sent: " + postData);

					try {
						ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
								.type("application/json").post(ClientResponse.class, postData);
						logger.info("MysqlConnectionMonitor POST response: " + response);
					} catch (Exception ex) {

						logger.error(" MysqlConnectionMonitor POST ERROR:" + ex.toString());
					}

				}
				int timeinterval = cpm.buildSchedule(scheduleType, sleepInterval);

				if (timeinterval != 0) {
					Thread.sleep(timeinterval);
				}

			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("MysqlConnectionMonitor threw error, full stack trace follows:" + e1);
			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("MysqlConnectionMonitor threw error, full stack trace follows:" + e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("MysqlConnectionMonitor threw error, full stack trace follows:" + e3);
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
			logger.error("MysqlConnectionMonitor Threw ERROR:", ex.toString());

		}

	}

	public void updateData()
			throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException {

		MysqlConnection mysqlconn = new MysqlConnection();
		Connection conn = null;
		Statement ps = null;
		ResultSet rs = null;

		try {
			conn = mysqlconn.beanConnection(ConnectionString);
			ps = (Statement) conn.createStatement();

			rs = (ResultSet) ps.executeQuery("show status where `variable_name` = 'Threads_connected'");

			while (rs.next()) {
				queryResult = rs.getString(2);

			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			int lenoferrorstr = stackTrace.length();
			if (lenoferrorstr > 200) {
				queryResult = "Error:" + stackTrace.substring(0, 200);
			} else {
				queryResult = "Error:" + stackTrace.substring(0, lenoferrorstr - 1);

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

		String str_product = eElement.getAttribute("product");
		CommonPluginMessages cm = new CommonPluginMessages();
		String hostname = cm.RunLinuxCommand("hostname");

		InetAddress addr = InetAddress.getLocalHost();
		String IpAddress = addr.getHostAddress();

		System.currentTimeMillis();
		MysqlConnection mysqlconn = new MysqlConnection();
		String str_Result = mysqlconn.runMysqlQuery(ConnectionString, "select version()");
		String str_Version = "";
		if (!str_Result.contains("Error:") && str_Result != "") {
			str_Version = str_Result;
		}

		str_Result = mysqlconn.runMysqlQuery(ConnectionString, "SHOW VARIABLES LIKE '%version_comment%'");
		String str_Edition = "";

		if (!str_Result.contains("Error:") && str_Result != "") {
			str_Edition = str_Result;
		}
		String status;
		if (queryResult.contains("Too many connections")) {

			resultStatus = "Instance connection count is reached the maximum count: " + str_instanceName;
			resultCode = 0;

			status = "ReachedMaxConnectionLimit";

		} else if (queryResult.contains("Error:")) {

			resultStatus = "Instance connection count is not returned: " + str_instanceName;
			status = "Down";
			resultCode = 0;

		} else {
			resultStatus = "Instance connection count returned for Instance: " + str_instanceName;
			resultCode = Integer.parseInt(queryResult);
			status = "Up";
		}

		str_product = "MySQL";
		postData = "{\"Data\"" + ":" + "\"\\u003cDatabaseConnectionMonitorPlugin ";
		postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
		postData = postData + " product=\\\"" + str_product + "\\\"";
		postData = postData + " productVersion=\\\"" + str_Version + "\\\"";
		postData = postData + " productLevel=\\\"\\\"";
		postData = postData + " productEdition=\\\"" + str_Edition + "\\\"";
		postData = postData + " metricInstanceId=\\\"" + str_metricInstanceId + "\\\"";
		postData = postData + " label=\\\"" + str_label + "\\\"";
		postData = postData + " resultCode=\\\"" + resultCode + "\\\"";
		postData = postData + " resultMessage=\\\"" + resultStatus + "\\\"";
		postData = postData + " instanceName=\\\"" + str_instanceName + "\\\"";
		postData = postData + " instanceStatus=\\\"" + status + "\\\" /\\u003e\"" + ",";
		postData = postData + " \"Hostname\"" + ":\"" + hostname + "\"," + "\"IpAddress\"" + ":\"" + IpAddress + "\",";
		postData = postData + " \"ServerId\"" + ":\"" + serverID + "\"," + "\"TenantId\"" + ":"
				+ "\"1a19a18a-846c-49da-93c1-8948afdc0151\"";
		postData = postData + "," + "\"Timestamp\"" + ":\"" + "\\/Date(" + System.currentTimeMillis() + ")\\/" + "\"";
		postData = postData + "," + "\"Id\":null" + "," + "\"PopReceipt\":0" + "," + "\"DequeueCount\":0}";
		// }
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

	public static void javaMYSQLConnect() {
		System.setProperty("javax.net.ssl.keyStore", "/var/datavail/mykeystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "test123");
		System.setProperty("javax.net.ssl.trustStore", "/var/datavail/mytruststore");
		System.setProperty("javax.net.ssl.trustStorePassword", "test123");

		String user = "delta";
		String password = "root";
		String URL = "jdbc:mysql://localhost:3306" +

				"?verifyServerCertificate=true" + "&useSSL=true" + "&requireSSL=true";
		try {

			Connection conn = DriverManager.getConnection(URL, user, password);

			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SHOW DATABASES");

			while (rs.next()) {
				String dbname = rs.getString("Database");
				// System.out.format("%s\n", dbname);
			}

			st.close();

		} catch (Exception ex) {
			// ex.printStackTrace();
			// System.out.println("Failed");
		}
	}

}
