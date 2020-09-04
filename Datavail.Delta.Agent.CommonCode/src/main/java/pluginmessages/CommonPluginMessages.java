package pluginmessages;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import connection.MysqlConnection;
/**
 * @File_Desc:Common Code Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :AgentApplication
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class CommonPluginMessages {

	String hostDetails;
	String Timestamp = "", Version = "", Edition = "", Header_Message = "";
	String hostname = "", serverid = "", Footer_Message = "";
	String CheckIn_Message = "";

	public String headermessage(String adapterclass) throws ClassNotFoundException, SQLException {
		MysqlConnection mysqlconn = new MysqlConnection();
		Connection Con = mysqlconn.beanConnection(hostDetails);
		Statement st = (Statement) Con.createStatement();
		ResultSet rs1 = null, rs2 = null, rs3 = null;

		try {
			String query1 = "select now()";
			rs1 = (ResultSet) st.executeQuery(query1);
			while (rs1.next()) {

				Timestamp = rs1.getString(1);

			}
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ssssss'Z'");
			SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = (Date) output.parse(Timestamp);
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

			String str_Data = eElement.getAttribute("Data");

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

	public String footermessage() throws ClassNotFoundException, SQLException, UnknownHostException {

		MysqlConnection mysqlconn = new MysqlConnection();
		Connection Con = mysqlconn.startconnection();
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

	public String checkinmessage(String adapterclass) throws ClassNotFoundException, SQLException {
		try {

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

			String str_Data = eElement.getAttribute("Data");

			String str_metricInstanceId = eElement.getAttribute("Id");

			String str_label = eElement.getAttribute("Label");

			CheckIn_Message = "MetricInstanceId:" + " " + str_metricInstanceId + " " + "Label:" + " " + str_label + " "
					+ "(Data:" + " " + str_Data + ")";

		}

		catch (Exception e) {

			System.err.println("Error: " + e.getMessage());

		}

		return CheckIn_Message;

	}

	public String serverId() throws ClassNotFoundException, SQLException {

		MysqlConnection mysqlconn = new MysqlConnection();
		Connection Con = mysqlconn.beanConnection(hostDetails);
		Statement st = (Statement) Con.createStatement();
		ResultSet rs1 = null;
		try {
			String query1 = "show variables Like '%server_uuid%'";
			rs1 = (ResultSet) st.executeQuery(query1);
			while (rs1.next()) {

				serverid = rs1.getString(2);

			}
		} catch (Exception e) {

			System.err.println("Error: " + e.getMessage());

		}
		return serverid;

	}

	/**
	 * @return the hostDetails
	 */
	public String getHostDetails() {
		return hostDetails;
	}

	/**
	 * @param hostDetails
	 *            the hostDetails to set
	 */
	public void setHostDetails(String hostDetails) {
		this.hostDetails = hostDetails;
	}

}
