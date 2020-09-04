
package oraclelistenerstatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

import connection.AES;
import connection.MysqlConnection;
import connection.OracleConnection;
import pluginmessages.CommonPluginMessages;
/**
 * @File_Desc: Oracle Listener Status Monitor Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :OracleListenerStatus
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class OracleListenerStatus extends BasePlugin {
	private String name;
	private int sleepInterval;
	private int scheduleType;
	private String ConnectionString;
	private String resultStatus = "";

	String postData = "";
	private String thershold;
	String serverID = "";
	String status = "";
	int resultCode = 0;
	private String metricInstanceId;

	@Override
	public void run() {
		try {
			CommonPluginMessages cpm = new CommonPluginMessages();

			serverID = cpm.serverId();

			MysqlConnection oracleconn = new MysqlConnection();

			WebResource webResource = oracleconn.client();

			while (true) {

				postData = getPostData();
				if (!postData.isEmpty()) {
					logger.info("OracleListenerStatus DATA sent: " + postData);

					try {
						ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
								.type("application/json").post(ClientResponse.class, postData);
						logger.info("OracleListenerStatus POST response: " + response);
					}

					catch (Exception ex) {

						logger.error(" OracleListenerStatus POST ERROR:" + ex.toString());
					}
				}
				int timeinterval = cpm.buildSchedule(scheduleType, sleepInterval);

				if (timeinterval != 0) {
					Thread.sleep(timeinterval);
				}

			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("OracleListenerStatus threw error, full stack trace follows:" + e1);
			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("OracleListenerStatus threw error, full stack trace follows:" + e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("OracleListenerStatus threw error, full stack trace follows:" + e3);
			// e3.printStackTrace();
		} catch (XPathExpressionException e) {
			// e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// e.printStackTrace();
		} catch (SAXException e) {
			// e.printStackTrace();
		} catch (Exception ex) {
			logger.error("OracleListenerStatus Threw ERROR:" + ex.toString());

		}

	}

	public void updateData(String strServiceName) throws Exception {

		OracleConnection oracleconn = new OracleConnection();
		CommonPluginMessages cms = new CommonPluginMessages();
		String stresult = "";
		String ConnString = "";

		final String secretKey = "$1234%&Key%";

		AES obj = new AES();
		ConnString = ConnectionString.trim();
		ConnString = obj.decrypt(ConnString, secretKey);
		String newServicenames = "";

		try {
			int startindex = 0;
			int endindex = 0;

			startindex = ConnString.lastIndexOf("SERVICE_NAME=");
			endindex = ConnString.lastIndexOf("User Id=");
			String latest = ConnString.substring(startindex, endindex - 4);
			latest.trim();
			String newServicename = latest.replace("SERVICE_NAME=", "");
			newServicenames = newServicename.replaceAll("\"", "");
			newServicenames.trim();
		} catch (Exception ex) {

		}
		String str_Servicename = newServicenames;

		// ="jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=localhost)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=TST11G)));User
		// Id= Testddd;Password=Testddd;";
		String ConnectionStringnew = "";

		String[] strArr = strServiceName.split(",");
		resultCode = 0;

		// resultStatus = "Listener " + strServiceName + "is Up";

		if (strServiceName.contains(",")) {
			resultStatus = " Listener : " + strServiceName + "are up";
		} else {
			resultStatus = " Listener : " + strServiceName + "is up";
		}

		for (int i = 0; i < strArr.length; i++) {
			ConnectionStringnew = ConnString.replaceAll(str_Servicename, strArr[i]);

			status = oracleconn.runOracleQuery1(ConnectionStringnew, "select 1 from dual");

			if (status.contains("ORA-01017:")) {

			} else if (status.equals("1")) {

			}

			else if (status.contains("ORA-")) {
				resultCode = 1;
				resultStatus = "Down";

				if (stresult == "") {

					stresult = strArr[i];
				} else {
					stresult = stresult + "," + strArr[i];
				}
			}

			else {
				resultCode = 1;
				resultStatus = "Down";

				if (stresult == "") {

					stresult = strArr[i];
				} else {
					stresult = stresult + "," + strArr[i];
				}

			}
		}
		if (resultCode == 1) {
			if (stresult.contains(",")) {
				resultStatus = "Listener : " + stresult + "are " + resultStatus;
			} else {
				resultStatus = "Listener : " + stresult + "is " + resultStatus;
			}
		}

	}

	public String getPostData() throws Exception {

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

		startindex = newData.lastIndexOf("ServiceName");
		endindex = newData.lastIndexOf("Label");
		latest = newData.substring(startindex, endindex);
		latest.trim();
		newInstanceName = latest.replace("ServiceName=", "");
		newInstanceNames = newInstanceName.replaceAll("\"", "");
		newInstanceNames.trim();
		String strServiceName = newInstanceNames;

		CommonPluginMessages cm = new CommonPluginMessages();
		String hostname = cm.RunLinuxCommand("hostname");

		InetAddress addr = InetAddress.getLocalHost();
		String IpAddress = addr.getHostAddress();

		System.currentTimeMillis();

		String pd = "";

		String str_Version = "";

		String pl = "";
		String pe = "";

		updateData(strServiceName);

		postData = "{\"Data\"" + ":" + "\"\\u003cListenerStatusPluginOutput ";
		postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
		postData = postData + " product=\\\"" + pd.replaceAll("\t", " ") + "\\\"";
		postData = postData + " productVersion=\\\"" + str_Version.replaceAll("\t", " ") + "\\\"";
		postData = postData + " productLevel=\\\"" + pl.replaceAll("\t", " ") + "\\\"";
		postData = postData + " productEdition=\\\"" + pe.replaceAll("\t", " ") + "\\\"";
		postData = postData + " metricInstanceId=\\\"" + str_metricInstanceId + "\\\"";
		postData = postData + " label=\\\"" + str_label + "\\\"";
		postData = postData + " instanceName=\\\"" + str_instanceName + "\\\"";
		postData = postData + " resultCode=\\\"" + resultCode + "\\\"";
		postData = postData + " resultMessage=\\\"" + resultStatus + "\\\" /\\u003e\"" + ",";

		postData = postData + " \"Hostname\"" + ":\"" + hostname + "\"," + "\"IpAddress\"" + ":\"" + IpAddress + "\",";
		postData = postData + " \"ServerId\"" + ":\"" + serverID + "\"," + "\"TenantId\"" + ":"
				+ "\"1a19a18a-846c-49da-93c1-8948afdc0151\"";
		postData = postData + "," + "\"Timestamp\"" + ":\"" + "\\/Date(" + System.currentTimeMillis() + ")\\/" + "\"";
		postData = postData + "," + "\"Id\":null" + "," + "\"PopReceipt\":0" + "," + "\"DequeueCount\":0}";
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
