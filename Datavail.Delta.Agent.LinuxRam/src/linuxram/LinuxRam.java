package linuxram;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
 * @File_Desc: Linux Ram info Plugin  Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :LinuxRam
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class LinuxRam extends BasePlugin {
	private String name;
	private int sleepInterval;
	private int scheduleType;
	private String ConnectionString;
	private String resultStatus = "";
	private String thershold;
	private long _totalPhysicalMemoryBytes;
	private String _totalPhysicalMemoryBytesFriendly;
	private String _availablePhysicalMemoryBytesFriendly;
	private long _totalVirtualMemoryBytes;
	private String _totalVirtualMemoryBytesFriendly;
	private String _availableVirtualMemoryBytesFriendly;
	private long _availablePhysicalMemoryBytes;
	private long _availableVirtualMemoryBytes;
	private long _percentagePhysicalFree;
	private long _percentageVirtualFree;
	private String metricInstanceId;
	String serverID;

	@Override
	public void run() {
		try {
			CommonPluginMessages cpm = new CommonPluginMessages();

			serverID = cpm.serverId();
			WebResource webResource = cpm.getURL("Linux: RAM");

			while (true) {

				String postData = getPostData();
				if (!postData.isEmpty()) {
					logger.info("LinuxRam DATA sent: " + postData);

					try {
						ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
								.type("application/json").post(ClientResponse.class, postData);
						logger.info("LinuxRam POST response: " + response);
					} catch (Exception ex) {

						logger.error(" LinuxRam Posting Error:"+ ex.toString());
					}
				}
				int timeinterval = cpm.buildSchedule(scheduleType, sleepInterval);

				if (timeinterval != 0) {
					Thread.sleep(timeinterval);
				}

			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("LinuxRam threw error, full stack trace follows:"+ e1);
			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("LinuxRam threw error, full stack trace follows:"+ e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("LinuxRam threw error, full stack trace follows:"+ e3);
			// e3.printStackTrace();
		} catch (XPathExpressionException e) {
			// e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// e.printStackTrace();
		} catch (SAXException e) {
			// e.printStackTrace();
		} catch (Exception ex) {
			logger.error("LinuxRam Threw ERROR:"+ ex.toString());

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
		String str_instanceName = eElement.getAttribute("InstanceName");

		String str_product = eElement.getAttribute("product");
		CommonPluginMessages cm = new CommonPluginMessages();
		String hostname = cm.RunLinuxCommand("hostname");

		InetAddress addr = InetAddress.getLocalHost();
		String IpAddress = addr.getHostAddress();

		String serverid = cm.serverId();

		String ServerId = serverid;

		System.currentTimeMillis();
		CommonPluginMessages cpm = new CommonPluginMessages();

		String cmd = "free | grep  Mem";
		String postData = "";
		String memory = cpm.RunLinuxGrepCommand(cmd);

		if (memory.contains("Error:")) {
			// to do check agent error status
			String errorPostData = cpm.buildErrorString("LinuxRam", "PostData", memory, ServerId, str_metricInstanceId,
					formattedTime, str_product, IpAddress);

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

			memory = memory.replaceAll("Mem:", "");
			memory = memory.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = memory.split(" ");

			_totalPhysicalMemoryBytes = Long.parseLong(strArr[0]);
			_availablePhysicalMemoryBytes = Long.parseLong(strArr[5]);
			cmd = "free | grep  Swap";
			String swapMemory = cpm.RunLinuxGrepCommand(cmd);
			swapMemory = swapMemory.replaceAll("Swap:", "");

			swapMemory = swapMemory.trim().replaceAll("\\s{2,}", " ");
			String[] strArr1 = swapMemory.split(" ");

			_totalVirtualMemoryBytes = Long.parseLong(strArr1[0]);
			_availableVirtualMemoryBytes = 0;
			_percentagePhysicalFree = 0;
			// if (_totalPhysicalMemoryBytes > 0)
			_percentagePhysicalFree = (_availablePhysicalMemoryBytes * 100L / _totalPhysicalMemoryBytes);

			_totalPhysicalMemoryBytesFriendly = convertSize(_totalPhysicalMemoryBytes);

			_totalVirtualMemoryBytesFriendly = convertSize(_totalVirtualMemoryBytes);

			_availablePhysicalMemoryBytesFriendly = convertSize(_availablePhysicalMemoryBytes);
			_availableVirtualMemoryBytesFriendly = convertSize(_availableVirtualMemoryBytes);
			postData = "{\"Data\"" + ":" + "\"\\u003cRamPluginOutput ";
			postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
			postData = postData + " product=\\\"" + str_product + "\\\"";
			postData = postData + " productVersion=\\\"" + "" + "\\\"";
			postData = postData + " productLevel=\\\"\\\"";
			postData = postData + " productEdition=\\\"" + "" + "\\\"";
			postData = postData + " metricInstanceId=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " label=\\\"" + str_label + "\\\"";
			postData = postData + " totalPhysicalMemoryBytes=\\\"" + _totalPhysicalMemoryBytes * 1024 + "\\\"";
			postData = postData + " totalPhysicalMemoryFriendly=\\\"" + _totalPhysicalMemoryBytesFriendly + "\\\"";
			postData = postData + " totalVirtualMemoryBytes=\\\"" + _totalVirtualMemoryBytes * 1024 + "\\\"";
			postData = postData + " totalVirtualMemoryFriendly=\\\"" + _totalVirtualMemoryBytesFriendly + "\\\"";
			postData = postData + " availablePhysicalMemoryBytes=\\\"" + _availablePhysicalMemoryBytes * 1024 + "\\\"";
			postData = postData + " availablePhysicalMemoryFriendly=\\\"" + _availablePhysicalMemoryBytesFriendly
					+ "\\\"";
			postData = postData + " availableVirtualMemoryBytes=\\\"" + _availableVirtualMemoryBytes * 1024 + "\\\"";
			postData = postData + " availableVirtualMemoryFriendly=\\\"" + _availableVirtualMemoryBytesFriendly
					+ "\\\"";
			postData = postData + " percentagePhysicalMemoryAvailable=\\\"" + _percentagePhysicalFree + "\\\"";
			postData = postData + " percentageVirtualMemoryAvailable=\\\"" + _percentageVirtualFree + "\\\" /\\u003e\""
					+ ",";

			postData = postData + " \"Hostname\"" + ":\"" + hostname + "\"," + "\"IpAddress\"" + ":\"" + IpAddress
					+ "\",";
			postData = postData + " \"ServerId\"" + ":\"" + ServerId + "\"," + "\"TenantId\"" + ":"
					+ "\"1a19a18a-846c-49da-93c1-8948afdc0151\"";
			postData = postData + "," + "\"Timestamp\"" + ":\"" + "\\/Date(" + System.currentTimeMillis() + ")\\/"
					+ "\"";
			postData = postData + "," + "\"Id\":null" + "," + "\"PopReceipt\":0" + "," + "\"DequeueCount\":0}";
		}
		return postData;
	}

	public static String convertSize(long size) {
		String hrSize = null;

		double b = size;
		double k = size / 1024.0;
		double m = ((size / 1024.0) / 1024.0);
		double g = (((size / 1024.0) / 1024.0) / 1024.0);
		double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

		DecimalFormat dec = new DecimalFormat("0.00");

		if (t > 1) {
			hrSize = dec.format(t).concat(" TB");
		} else if (g > 1) {
			hrSize = dec.format(g).concat(" GB");
		} else if (m > 1) {
			hrSize = dec.format(m).concat(" MB");
		} else if (k > 1) {
			hrSize = dec.format(k).concat(" KB");
		} else {
			hrSize = dec.format(b).concat(" Bytes");
		}

		return hrSize;
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
