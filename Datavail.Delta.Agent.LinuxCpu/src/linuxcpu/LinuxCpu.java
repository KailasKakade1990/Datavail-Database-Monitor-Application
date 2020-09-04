package linuxcpu;

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

import connection.MysqlConnection;
import pluginmessages.CommonPluginMessages;
/**
 * @File_Desc:Linux CPU info Plugin  Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :LinuxCpu
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class LinuxCpu extends BasePlugin {
	private String name;
	private int sleepInterval;
	private int scheduleType;
	private String ConnectionString;
	private String resultStatus = "";
	private String thershold;
	String serverID;
	private String metricInstanceId;

	@Override
	public void run() {
		try {
			CommonPluginMessages cpm = new CommonPluginMessages();

			serverID = cpm.serverId();

			WebResource webResource = cpm.getURL("Linux: CPU");

			while (true) {

				String postData = getPostData();

				if (!postData.isEmpty()) {
					logger.info("LinuxCpu DATA sent: " + postData);
					try {
						ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
								.type("application/json").post(ClientResponse.class, postData);
						logger.info("LinuxCpu POST response: " + response);
					} catch (Exception ex) {

						logger.error(" LinuxCpu POST ERROR:" + ex.toString());
					}
				}

				int timeinterval = cpm.buildSchedule(scheduleType, sleepInterval);

				if (timeinterval != 0) {
					Thread.sleep(timeinterval);
				}

			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("LinuxCpu threw error, full stack trace follows:" + e1);
			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("LinuxCpu threw error, full stack trace follows:" + e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("LinuxCpu threw error, full stack trace follows:" + e3);
			// e3.printStackTrace();
		} catch (XPathExpressionException e1) {
			logger.error("LinuxCpu threw error, full stack trace follows:" + e1);
			// e.printStackTrace();
		} catch (ParserConfigurationException e) {
			logger.error("LinuxCpu threw error, full stack trace follows: " + e);
			// e.printStackTrace();
		} catch (SAXException e) {
			// e.printStackTrace();
		} catch (Exception ex) {
			logger.error("LinuxCpu Threw ERROR:" + ex.toString());

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
		@SuppressWarnings("unused")
		String str_instanceName = eElement.getAttribute("InstanceName");

		String str_product = eElement.getAttribute("product");
		CommonPluginMessages cm = new CommonPluginMessages();
		String hostname = cm.RunLinuxCommand("hostname");

		InetAddress addr = InetAddress.getLocalHost();
		String IpAddress = addr.getHostAddress();

		System.currentTimeMillis();
		CommonPluginMessages cpm = new CommonPluginMessages();
		String postData = "";
		String cmd1 = "cat /proc/stat |grep 'cpu '";
		String cpuMemory = cpm.RunLinuxGrepCommand(cmd1);
		cpuMemory = cpuMemory.trim();
		if (cpuMemory.contains("Error:")) {
			// to do check agent error status
			String errorPostData = cpm.buildErrorString("LinuxCpu", "PostData", cpuMemory, serverID,
					str_metricInstanceId, formattedTime, str_product, IpAddress);

			if (!errorPostData.isEmpty()) {
				logger.info("AgentError DATA sent: " + errorPostData);

				String serverID = cpm.serverId();

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
			// String cmd = " grep 'cpu ' /proc/stat | awk
			// '{usage=($2+$4)*100/($2+$4+$5)} END {print usage }'";
			cpuMemory = cpuMemory.replace("cpu  ", "");
			String[] strArr = cpuMemory.split(" ");
			long user = Long.parseLong(strArr[0]);
			long nice = Long.parseLong(strArr[1]);
			long system = Long.parseLong(strArr[2]);
			long idle = Long.parseLong(strArr[3]);
			long iowait = Long.parseLong(strArr[4]);
			long irq = Long.parseLong(strArr[5]);
			long softirq = Long.parseLong(strArr[6]);
			long steal = Long.parseLong(strArr[7]);

			long totaltimesinceboot = (user + nice + system + idle + iowait + irq + softirq + steal);
			long totalidletimesinceboot = idle + iowait;
			long totalusagetimesinceboot = totaltimesinceboot - totalidletimesinceboot;
			long totalCPUpercentage = totalusagetimesinceboot * 100 / totaltimesinceboot;

			// cpuMemory = totalCPUpercentage;

			postData = "{\"Data\"" + ":" + "\"\\u003cCpuPluginOutput ";
			postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
			postData = postData + " product=\\\"" + str_product + "\\\"";
			postData = postData + " productVersion=\\\"" + "" + "\\\"";
			postData = postData + " productLevel=\\\"\\\"";
			postData = postData + " productEdition=\\\"" + "" + "\\\"";
			postData = postData + " metricInstanceId=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " label=\\\"" + str_label + "\\\"";
			postData = postData + " percentageCpuUsed=\\\"" + totalCPUpercentage + "\\\" /\\u003e\"" + ",";

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
