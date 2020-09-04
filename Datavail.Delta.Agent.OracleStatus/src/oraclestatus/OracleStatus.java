package oraclestatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
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
 * @File_Desc: Oracle Status Monitor Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :OracleStatus
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class OracleStatus extends BasePlugin {
	private String name;
	private int sleepInterval;
	private String dataStatusCode = "";
	private String dataStatus = "";
	private String header_message = "";
	private String ConnectionString;
	private String resultStatus = "";
	private int scheduleType;
	private String thershold;
	private String metricInstanceId;
	String status = "";
	int resultCode = 0;
	String serverid = "";
	String postData = "";
	CommonPluginMessages cpm = new CommonPluginMessages();

	@Override
	public void run() {
		try {

			serverid = cpm.serverId();
			WebResource webResource = cpm.getURL("Linux Oracle: Oracle Instance Status");

			while (true) {

				postData = getPostData();
				if (!postData.isEmpty()) {
					logger.info("OracleStatus DATA sent: " + postData);

					try {
						ClientResponse response = webResource.path("/{id}").queryParam("id", serverid)
								.type("application/json").post(ClientResponse.class, postData);
						logger.info("OracleStatus POST response: " + response);
					}

					catch (Exception ex) {

						logger.error("OracleStatus POST ERROR:"+ ex.toString());
					}

					int timeinterval = cpm.buildSchedule(scheduleType, sleepInterval);

					if (timeinterval != 0) {
						Thread.sleep(timeinterval);
					}

				}
			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("OracleStatus threw error, full stack trace follows:"+ e1);
			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("OracleStatus threw error, full stack trace follows:"+ e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("OracleStatus threw error, full stack trace follows:"+ e3);
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
			logger.error("OracleStatus Threw ERROR:"+ ex.toString());

		}

	}

	public void updateData()
			throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException {

		OracleConnection oracleconn = new OracleConnection();
		status = oracleconn.runOracleQuery(ConnectionString, "select 1 from dual");

		if (status.contains("Error:")) {
			resultCode = 1;
			resultStatus = "Down";

		} else if (status.equals("1")) {
			resultCode = 0;
			resultStatus = "Up";

		} else {

			resultCode = 1;
			resultStatus = "Down";
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

		String str_MetricInstanceId = eElement.getAttribute("Id");

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
		updateData();
		postData = "";
		if (status.contains("Error:")) {
			status = " ErrorMessage=\\\"" + status + "\\\"";
		} else {
			status = "";
		}
		postData = "{\"Data\"" + ":" + "\"\\u003cOracleInstanceStatusPluginOutput ";
		postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
		postData = postData + " product=\\\"" + pd.replaceAll("\t", " ") + "\\\"";
		postData = postData + " productVersion=\\\"" + str_Version.replaceAll("\t", " ") + "\\\"";
		postData = postData + " productLevel=\\\"" + pl.replaceAll("\t", " ") + "\\\"";
		postData = postData + " productEdition=\\\"" + pe.replaceAll("\t", " ") + "\\\"";
		postData = postData + " metricInstanceId=\\\"" + str_MetricInstanceId + "\\\"";
		postData = postData + " label=\\\"" + str_label + "\\\"";
		postData = postData + " resultCode=\\\"" + resultCode + "\\\"";
		postData = postData + " resultMessage=\\\"Instance Status is " + resultStatus + "\\\"";
		postData = postData + " instanceName=\\\"" + str_instanceName + "\\\"";
		postData = postData + status;
		postData = postData + " instanceStatus=\\\"" + resultStatus + "\\\" /\\u003e\"" + ",";
		postData = postData + " \"Hostname\"" + ":\"" + hostname + "\"," + "\"IpAddress\"" + ":\"" + IpAddress + "\",";
		postData = postData + " \"ServerId\"" + ":\"" + serverid + "\"," + "\"TenantId\"" + ":"
				+ "\"1a19a18a-846c-49da-93c1-8948afdc0151\"";
		postData = postData + "," + "\"Timestamp\"" + ":\"" + "\\/Date(" + System.currentTimeMillis() + ")\\/" + "\"";
		postData = postData + "," + "\"Id\":null" + "," + "\"PopReceipt\":0" + "," + "\"DequeueCount\":0}";

		return postData;

	}

	public String getAgentLogMessage() {

		java.util.Date date = new java.util.Date();
		java.sql.Timestamp datetime = new Timestamp(date.getTime());

		String Message = "OracleInstanceStatusPluginOutput" + header_message + dataStatus + dataStatusCode;

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

	public void setMetricInstanceId(String metricInstanceId) {
		this.metricInstanceId = metricInstanceId;
	}

}
