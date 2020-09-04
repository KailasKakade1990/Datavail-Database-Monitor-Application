package connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/** Logger Imports **/
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import pluginmessages.CommonPluginMessages;
/**
 * @File_Desc:Agent Connection Oracle JDBC and MYSQL JDBC
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :MysqlConnection
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 * 
 */
public class MysqlConnection {
	protected static Logger logger = LogManager.getLogger(MysqlConnection.class);

	String linux_host_name = "", linuxmysql_user_name = "", linuxmysql_pass_word = "";
	String linux_host_name1 = "", linuxmysql_user_name1 = "", linuxmysql_pass_word1 = "";
	String linux_hostname = "", linux_username = "", linux_password = "", linux_command = "";
	String ndbcluster_hostname = "", ndbcluster_username = "", ndbcluster_password = "", ndbcluster_command = "";
	String client_url = "", client_url1 = "";

	String Timestamp = "", Version = "", Edition = "", Header_Message = "";
	String hostname = "", serverid = "", Footer_Message = "";

	public Connection beanConnection(String ConnectionString) throws ClassNotFoundException, SQLException {
		try {

			final String secretKey = "$1234%&Key%";

			AES obj = new AES();
			ConnectionString = ConnectionString.trim();
			ConnectionString = obj.decrypt(ConnectionString, secretKey);
			ConnectionString.trim();
			String[] strArr = ConnectionString.split(";");
			Map<String, String> map = new HashMap<String, String>();
			for (String s1 : strArr) {
				String[] str = s1.split("=");
				map.put(str[0], str[1]);
			}

			linux_host_name = map.get("linuxmysqlhost");
			linuxmysql_user_name = map.get("linuxmysqluser");
			linuxmysql_pass_word = map.get("linuxmysqlpass");
			linuxmysql_pass_word = linuxmysql_pass_word.trim();
		} catch (Exception e) {
			// e.printStackTrace();
		}

		Class.forName("com.mysql.jdbc.Driver");
		Connection con = (Connection) DriverManager.getConnection(linux_host_name, linuxmysql_user_name,
				linuxmysql_pass_word);
		return con;
	}

	public Connection startconnection() throws ClassNotFoundException, SQLException {
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			linux_host_name = properties.getProperty("linuxmysqlhost");
			linuxmysql_user_name = properties.getProperty("linuxmysqluser");
			linuxmysql_pass_word = properties.getProperty("linuxmysqlpass");

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		Class.forName("com.mysql.jdbc.Driver");
		Connection con = (Connection) DriverManager.getConnection(linux_host_name, linuxmysql_user_name,
				linuxmysql_pass_word);
		return con;
	}

	public Channel remoteconnection() throws JSchException, IOException {
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			linux_hostname = properties.getProperty("linuxhost");
			linux_username = properties.getProperty("linuxuser");
			linux_password = properties.getProperty("linuxpass");
			linux_command = properties.getProperty("command");
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		JSch jsch = new JSch();
		Session session = jsch.getSession(linux_username, linux_hostname, 22);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setPassword(linux_password);
		session.connect();
		Channel channel = (Channel) session.openChannel("exec");
		((ChannelExec) channel).setCommand(linux_command);
		((ChannelExec) channel).setInputStream(null);
		((ChannelExec) channel).setErrStream(System.err);
		return channel;

	}

	public Channel Ndbclusterremoteconnection() throws JSchException, IOException {
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			ndbcluster_hostname = properties.getProperty("ndbcluster_host");
			ndbcluster_username = properties.getProperty("ndbcluster_user");
			ndbcluster_password = properties.getProperty("ndbcluster_pass");
			ndbcluster_command = properties.getProperty("ndbcluster_command");

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		JSch jsch = new JSch();
		Session session = jsch.getSession(ndbcluster_username, ndbcluster_hostname, 22);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setPassword(ndbcluster_password);
		session.connect();

		Channel channel = (Channel) session.openChannel("exec");
		((ChannelExec) channel).setCommand(ndbcluster_command);
		((ChannelExec) channel).setInputStream(null);
		((ChannelExec) channel).setErrStream(System.err);
		return channel;

	}

	public WebResource client() throws ClassNotFoundException, SQLException, UnknownHostException {

		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			client_url = properties.getProperty("clienturl");
			// logger.info("client_url: " + client_url);

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		} catch (Exception e) {

			logger.info("Web Resource ERROR: " + e.toString());

		}

		Client client = Client.create();

		WebResource webResource = client.resource(client_url);

		return webResource;
	}

	public WebResource client1() throws ClassNotFoundException, SQLException {
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			client_url1 = properties.getProperty("clienturl1");
			// logger.info("client_url1: " + client_url1);

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

	public String runMysqlQuery(String ConnectionString, String cmdString) {
		MysqlConnection mysqlconn = new MysqlConnection();
		String queryResult = "";
		Connection conn = null;
		Statement ps = null;
		ResultSet rs = null;
		try {
			conn = mysqlconn.beanConnection(ConnectionString);
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

	public String runMysqlQueryReturnColumn(String ConnectionString, String cmdString, int columnNumber) {
		MysqlConnection mysqlconn = new MysqlConnection();
		String queryResult = "";
		try {
			Connection Con = mysqlconn.beanConnection(ConnectionString);
			Statement st = (Statement) Con.createStatement();
			ResultSet rs = null;

			rs = (ResultSet) st.executeQuery(cmdString);
			while (rs.next()) {
				queryResult = rs.getString(columnNumber);
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
		}
		return queryResult;
	}

	public String headermessage(String adapterclass, String ConnectionString)
			throws ClassNotFoundException, SQLException {
		MysqlConnection mysqlconn = new MysqlConnection();
		Connection Con = mysqlconn.beanConnection(ConnectionString);
		Statement st = (Statement) Con.createStatement();
		ResultSet rs2 = null, rs3 = null;

		try {

			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ssssss'Z'");
			// SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd
			// HH:mm:ss");
			Date date = new Date();

			String formattedTime = dateformat.format(date);

			String datetimestamp = "timestamp=" + "\\" + "\"" + formattedTime + "\\" + "\"";

			String query2 = "select version()";
			rs2 = (ResultSet) st.executeQuery(query2);
			while (rs2.next()) {

				Version = rs2.getString(1);

			}
			String productVersion = "productVersion=" + "\\" + "\"" + Version + "\\" + "\"";

			String productLevel = "productLevel=" + "\\" + "\"" + "\\" + "\"";

			String query3 = "SHOW VARIABLES LIKE '%version_comment%'";
			rs3 = (ResultSet) st.executeQuery(query3);
			while (rs3.next()) {

				Edition = rs3.getString(2);

			}
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

			// String str_Data = eElement.getAttribute("Data");

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

			System.err.println("Error: " + e.getMessage());

		}

		return Header_Message;

	}

	public String footermessage(String ConnectionString)
			throws ClassNotFoundException, SQLException, UnknownHostException {

		MysqlConnection mysqlconn = new MysqlConnection();
		Connection Con = mysqlconn.beanConnection(ConnectionString);
		Statement st = (Statement) Con.createStatement();
		ResultSet rs1 = null, rs2 = null;

		String query1 = "select @@Hostname";
		rs1 = (ResultSet) st.executeQuery(query1);

		while (rs1.next()) {

			hostname = rs1.getString(1);

		}
		String Hostname = "\"Hostname\":\"" + hostname + "\"" + ",";

		InetAddress addr = InetAddress.getLocalHost();
		String IpAddress = "\"IpAddress\":\"" + addr.getHostAddress() + "\"" + ",";

		String query2 = "show variables Like '%server_uuid%'";
		rs2 = (ResultSet) st.executeQuery(query2);
		while (rs2.next()) {

			serverid = rs2.getString(2);

		}
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