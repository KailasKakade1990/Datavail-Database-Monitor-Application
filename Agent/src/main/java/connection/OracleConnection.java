
package connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import pluginmessages.CommonPluginMessages;
/**
 * @File_Desc:Agent Connection String Encryption Oracle JDBC and MYSQL JDBC
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :OracleConnection
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 * 
 */
public class OracleConnection {
	protected static Logger logger = LogManager.getLogger(MysqlConnection.class);

	String productVersion = "";
	String productLevel = "";

	public Connection beanConnection(String ConnectionString) throws Exception {

		final String secretKey = "$1234%&Key%";

		AES obj = new AES();

		ConnectionString = ConnectionString.trim();
		ConnectionString = obj.decrypt(ConnectionString, secretKey);
		String linux_host_name = "";
		String linuxoracle_pass_word = "";
		String linuxoracle_user_name = "";
		// connection string
		try {
			ConnectionString.trim();
			String[] strArr = ConnectionString.split(";");
			Map<String, String> map = new HashMap<String, String>();
			for (String s1 : strArr) {
				try {
					String[] str = s1.split("=");
					map.put(str[0], str[1]);

				} catch (Exception e) {

				}
			}

			linux_host_name = map.get("linuxoraclehost");
			linuxoracle_user_name = map.get("linuxoracleuser");
			linuxoracle_pass_word = map.get("linuxoraclepass");

		} catch (Exception e) {
			// e.printStackTrace();
		}
		Class.forName("oracle.jdbc.OracleDriver");

		Connection con = (Connection) DriverManager.getConnection(linux_host_name, linuxoracle_user_name,
				linuxoracle_pass_word);
		return con;
	}

	public Connection beanListnerConnection(String ConnectionString) throws Exception {

		String linux_host_name = "";
		String linuxoracle_pass_word = "";
		String linuxoracle_user_name = "";

		try {

			ConnectionString.trim();

			String[] strArr = ConnectionString.split(";");

			linux_host_name = strArr[0];

			linuxoracle_user_name = strArr[1].replaceAll("User Id=", "");
			linuxoracle_pass_word = strArr[2].replaceAll("Password=", "");

		} catch (Exception e) {
		}
		Class.forName("oracle.jdbc.driver.OracleDriver");

		Connection con = (Connection) DriverManager.getConnection(linux_host_name, linuxoracle_user_name,
				linuxoracle_pass_word);
		return con;
	}

	public WebResource client() throws ClassNotFoundException, SQLException {
		String client_url = "";
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			client_url = properties.getProperty("clienturl");

		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		} catch (Exception e) {

			logger.info("Web Resource ERROR: " + e.toString());

		}

		Client client = Client.create();
		WebResource webResource = client.resource(client_url);

		return webResource;
	}

	public WebResource client1() throws ClassNotFoundException, SQLException {
		String client_url1 = "";
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			client_url1 = properties.getProperty("clienturl1");

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		} catch (Exception e) {

			logger.info("Web Resource ERROR: " + e.toString());

		}

		Client client = Client.create();
		WebResource webResource1 = client.resource(client_url1);

		return webResource1;
	}

	String Timestamp = "", Version = "", Edition = "", Header_Message = "";
	String hostname = "", serverid = "", Footer_Message = "";
	String CheckIn_Message = "";

	public String runOracleQuery(String connstring, String cmdString) throws SQLException {
		OracleConnection oracleconn = new OracleConnection();
		String queryResult = "";
		Connection conn = null;
		Statement ps = null;
		ResultSet rs = null;
		try {

			conn = oracleconn.beanConnection(connstring);

			ps = (Statement) conn.createStatement();

			rs = (ResultSet) ps.executeQuery(cmdString);
			while (rs.next()) {
				queryResult = rs.getString(1);
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
		return queryResult;
	}

	public String runOracleQuery1(String ConnectionString, String cmdString) throws SQLException {
		OracleConnection oracleconn = new OracleConnection();
		String queryResult = "";
		Connection conn = null;
		Statement ps = null;
		ResultSet rs = null;
		try {
			conn = oracleconn.beanListnerConnection(ConnectionString);
			ps = (Statement) conn.createStatement();

			rs = (ResultSet) ps.executeQuery(cmdString);
			while (rs.next()) {
				queryResult = rs.getString(1);
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
		return queryResult;
	}

	public String headermessage(String adapterclass, String ConnectionString)
			throws ClassNotFoundException, SQLException, XPathExpressionException, ParserConfigurationException,
			InvalidPropertiesFormatException, IOException, SAXException {

		String datetimestamp = "";
		OracleConnection oracleconn = new OracleConnection();
		try {
			Connection Con1 = oracleconn.beanConnection(ConnectionString);
			Statement st = (Statement) Con1.createStatement();
			ResultSet rs2 = null, rs3 = null;

			String query2 = "select version from v$instance";
			rs2 = (ResultSet) st.executeQuery(query2);
			while (rs2.next()) {
				Version = rs2.getString(1);
			}
			productVersion = "productVersion=" + "\\" + "\"" + Version + "\\" + "\"";

			productLevel = "productLevel=" + "\\" + "\"" + "\\" + "\"";

			String query3 = "select platform_name from v$database";
			rs3 = (ResultSet) st.executeQuery(query3);
			while (rs3.next()) {
				Edition = rs3.getString(1);

			}

		} catch (Exception e) {

		}
		try {

			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ssssss'Z'");
			@SuppressWarnings("unused")
			SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();

			String formattedTime = dateformat.format(date);

			datetimestamp = "timestamp=" + "\\" + "\"" + formattedTime + "\\" + "\"";

			String productEdition = "productEdition=" + "\\" + "\"" + Edition + "\\" + "\"";

			File configfile = new File("config.xml");

			FileInputStream fileInput = new FileInputStream(configfile);

			Properties properties = new Properties();
			properties.loadFromXML(fileInput);

			String DeltaAgent_file = properties.getProperty("deltaagent");
			File fXmlFile = new File(DeltaAgent_file);

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			String xPathString = "/AgentConfiguration/MetricInstance[@AdapterClass='" + adapterclass + "']";

			XPath xPath = XPathFactory.newInstance().newXPath();
			XPathExpression xPathExpression = xPath.compile(xPathString);
			Node nNode = (Node) xPathExpression.evaluate(doc, XPathConstants.NODE);

			Element eElement = (Element) nNode;

			String str_metricInstanceId = eElement.getAttribute("Id");

			String str_label = eElement.getAttribute("Label");

			String str_resultCode = eElement.getAttribute("resultCode");

			String str_resultMessage = eElement.getAttribute("resultMessage");

			String str_product = eElement.getAttribute("product");

			Header_Message = " " + datetimestamp + " " + "metricInstanceId=" + str_metricInstanceId + " " + "label="
					+ str_label + " " + "resultCode=" + str_resultCode + " " + "resultMessage=" + str_resultMessage
					+ " " + "product=" + str_product + " " + productVersion + " " + productLevel + " " + productEdition
					+ " ";
		}

		catch (Exception e) {
		}

		return Header_Message;

	}

	public String footermessage(String ConnectionString)
			throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException {

		CommonPluginMessages cm = new CommonPluginMessages();
		hostname = cm.RunLinuxCommand("hostname");

		String Hostname = "\"Hostname\":\"" + hostname + "\"" + ",";

		InetAddress addr = InetAddress.getLocalHost();
		String IpAddress = "\"IpAddress\":\"" + addr.getHostAddress() + "\"" + ",";

		serverid = cm.serverId();

		String ServerId = "\"ServerId\":\"" + serverid + "\"" + ",";

		String TenantId = "\"TenantId\":\"1a19a18a-846c-49da-93c1-8948afdc0151\"" + ",";

		System.currentTimeMillis();
		String Timestamp = "\"Timestamp\":\"" + "\\" + "/" + "Date" + "(" + System.currentTimeMillis() + ")" + "\\"
				+ "/" + "\"" + "," + "\"Id\":null" + "," + "\"PopReceipt\":0" + "," + "\"DequeueCount\":0";

		Footer_Message = Footer_Message + Hostname + IpAddress + ServerId + TenantId + Timestamp + "}" + "\"" + ","
				+ Hostname + IpAddress + TenantId + Timestamp + "}";

		return Footer_Message;

	}

}
