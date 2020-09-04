package oraclelogwatcherinventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
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
 * @File_Desc: Oracle Log Watcher Inventory Monitor Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :OracleLogWatcherInventory
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class OracleLogWatcherInventory extends BasePlugin {
	private String name;
	private int sleepInterval;
	private String dataStatusCode = "";
	private String dataStatus = "";
	private String header_message = "";
	private String ConnectionString;
	private String instanceName = "";
	private String logwatcherPath = "";
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

			serverID = CPM.serverId();// hostid from linux system
			WebResource webResource = CPM.getURL("Linux Oracle: Log Watcher Inventory");

			while (true) {

				postData = getPostData();

				if (!postData.isEmpty()) {
					logger.info("OracleLogWatcherInventory DATA sent: " + postData);
					try {

						ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
								.type("application/json").post(ClientResponse.class, postData);
						logger.info("OracleLogWatcherInventory POST response: " + response);

					} catch (Exception ex) {

						logger.error("OracleLogWatcherInventory POST ERROR:"+ ex.toString());
					}
				}
				int timeinterval = CPM.buildSchedule(scheduleType, sleepInterval);

				if (timeinterval != 0) {
					Thread.sleep(timeinterval);
				}

			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("OracleLogWatcherInventory threw error, full stack trace follows:"+ e1);
			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("OracleLogWatcherInventory threw error, full stack trace follows:"+ e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("OracleLogWatcherInventory threw error, full stack trace follows:"+ e3);
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
			logger.error("OracleLogWatcherInventory Threw ERROR:"+ ex.toString());

		}

	}

	public void updateData()
			throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException {

		OracleConnection oracleconn = new OracleConnection();
		status = oracleconn.runOracleQuery(ConnectionString, "SELECT instance_name FROM v$instance");
		if (status.contains("Error:")) {

			instanceName = "";
		} else if (status != "") {

			instanceName = status;
		} else {

			instanceName = "";
		}
		status = oracleconn.runOracleQuery(ConnectionString,
				"select value from v$parameter where name='background_dump_dest'");
		if (status.contains("Error:")) {

			logwatcherPath = "";
		} else if (status != "") {

			logwatcherPath = status;
			if (instanceName != "") {
				logwatcherPath = logwatcherPath + "/" + "alert_" + instanceName + ".log";
			}
		} else {

			logwatcherPath = "";
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
		updateData();

		if (status.contains("Error:")) {
			// to do check agent error status
			String errorPostData = cm.buildErrorString("OracleLogWatcherInventory", "PostData", status, serverID,
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
			postData = "{\"Data\"" + ":" + "\"\\u003cLinOracleLogWatcherInventoryPlugin ";
			postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
			postData = postData + " product=\\\"" + pd.replaceAll("\t", " ") + "\\\"";
			postData = postData + " productVersion=\\\"" + str_Version.replaceAll("\t", " ") + "\\\"";
			postData = postData + " productLevel=\\\"" + pl.replaceAll("\t", " ") + "\\\"";
			postData = postData + " productEdition=\\\"" + pe.replaceAll("\t", " ") + "\\\"";
			postData = postData + " metricInstanceId=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " label=\\\"" + str_label + "\\\"";
			postData = postData + " log_error=\\\"" + logwatcherPath + "\\\"";
			postData = postData + " resultMessage=\\\"Oracle LogWatcher Inventory Plugin is running for the instance: "
					+ instanceName + "\\\"";
			postData = postData + " instanceName=\\\"" + instanceName + "\\\" /\\u003e\"" + ",";
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
	 * @return the dataStatusCode
	 */
	public String getDataStatusCode() {
		return dataStatusCode;
	}

	/**
	 * @param dataStatusCode
	 *            the dataStatusCode to set
	 */
	public void setDataStatusCode(String dataStatusCode) {
		this.dataStatusCode = dataStatusCode;
	}

	/**
	 * @return the dataStatus
	 */
	public String getDataStatus() {
		return dataStatus;
	}

	/**
	 * @param dataStatus
	 *            the dataStatus to set
	 */
	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}

	/**
	 * @return the header_message
	 */
	public String getHeader_message() {
		return header_message;
	}

	/**
	 * @param header_message
	 *            the header_message to set
	 */
	public void setHeader_message(String header_message) {
		this.header_message = header_message;
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
