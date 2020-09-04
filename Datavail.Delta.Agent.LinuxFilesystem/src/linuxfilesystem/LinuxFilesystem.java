package linuxfilesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

import pluginmessages.CommonPluginMessages;
/**
 * @File_Desc:linuxfilesystem info Plugin  Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :LinuxFilesystem
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class LinuxFilesystem extends BasePlugin {
	private String name;
	private int sleepInterval;

	private String ConnectionString;
	private String result = "";

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
			WebResource webResource = CPM.getURL("Linux: Filesystem");
			while (true) {
				// updateData();
				postData = getPostData();
				if (!postData.isEmpty()) {

					logger.info("LinuxFilesystem DATA sent: " + postData);

					try {
						ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
								.type("application/json").post(ClientResponse.class, postData);
						logger.info("LinuxFilesystem POST response: " + response);
					} catch (Exception ex) {

						logger.error(" LinuxFilesystem POST ERROR:"+ ex.toString());
					}
				}

				int timeinterval = CPM.buildSchedule(scheduleType, sleepInterval);

				if (timeinterval != 0) {
					Thread.sleep(timeinterval);
				}
				if (timeinterval == 0) {
					Thread.sleep(300 * 1000 * 60 * 60);
				}
			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("LinuxFilesystem threw error, full stack trace follows:"+ e1);
			// e1.sTrace();
		} catch (IOException e2) {
			logger.error("LinuxFilesystem threw error, full stack trace follows:"+e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("LinuxFilesystem threw error, full stack trace follows:"+ e3);
			// e3.printStackTrace();

		} catch (XPathExpressionException e) {

			// e.printStackTrace();
		} catch (ParserConfigurationException e) {

			// e.printStackTrace();
		} catch (SAXException e) {

			// e.printStackTrace();
		} catch (Exception ex) {
			logger.error("LinuxFilesystem Threw ERROR:"+ ex.toString());

		}
	}

	public void updateData(String str_label)
			throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException,
			XPathExpressionException, ParserConfigurationException, SAXException {
		try {
			result = "";
			String[] disk;
			String line;
			Process p;
			BufferedReader input;

			int startindex = 0;
			int endindex = 0;
			str_label = str_label.replace("Filesystem Status for '", "");
			startindex = 0;// str_label.lastIndexOf("(")+1;
			endindex = str_label.lastIndexOf("(") - 1;
			String latest = str_label.substring(startindex, endindex);
			latest.trim();
			// String lastValue = str_label.substring();
			p = Runtime.getRuntime().exec("df -h");

			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			input.readLine();

			while ((line = input.readLine()) != null) {
				disk = line.split("\\s+");

				if (latest.contentEquals(disk[5].trim())) {
					long fileSize = formatFileSize(disk[1]);

					result = result + " Filesystem=\\\"" + disk[0].trim() + "\\\"";
					result = result + " totalBytes=\\\"" + fileSize + "\\\"";
					result = result + " totalBytesFriendly=\\\"" + fileSize + "\\\"";
					result = result + " availableBytes=\\\"" + formatFileSize(disk[3]) + "\\\"";
					result = result + " availableBytesFriendly=\\\"" + formatFileSize(disk[3]) + "\\\"";
					result = result + " percentageAvailable=\\\"" + disk[4].replace("%", "").trim() + "\\\"";

					input.close();
				}
			}

		} catch (Exception ex) {

		}

	}

	public String getPostData() throws XPathExpressionException, InvalidPropertiesFormatException, IOException,
			ParserConfigurationException, SAXException, ClassNotFoundException, SQLException {
		postData = "";
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ssssss'Z'");
		SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String formattedTime = dateformat.format(date);
		String datetimestamp = "timestamp=" + "\\" + "\"" + formattedTime + "\\" + "\"";

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

		String str_instanceName = "";

		CommonPluginMessages cm = new CommonPluginMessages();
		String hostname = cm.RunLinuxCommand("hostname");

		InetAddress addr = InetAddress.getLocalHost();
		String IpAddress = addr.getHostAddress();

		System.currentTimeMillis();

		String pd = "";

		String str_Version = "";

		String pl = "";

		String pe = "";
		updateData(str_label);
		postData = "";
		if (result != "") {

			postData = "{\"Data\"" + ":" + "\"\\u003cFileSystemPluginOutput ";
			postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
			postData = postData + " product=\\\"" + str_product + "\\\"";
			postData = postData + " productVersion=\\\"" + "" + "\\\"";
			postData = postData + " productLevel=\\\"\\\"";
			postData = postData + " productEdition=\\\"" + "" + "\\\"";
			postData = postData + " metricInstanceId=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " label=\\\"" + str_label + "\\\"";
			postData = postData + result;
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

	public static long formatFileSize(String fileSize) {

		String lastValue = fileSize.substring(fileSize.length() - 1);
		lastValue = lastValue.trim();

		long size = (long) Double.parseDouble(fileSize.replace(lastValue, ""));

		long hrSize;

		long b = size;
		long k = size * 1024;
		long m = ((size * 1024) * 1024);
		long g = (((size * 1024) * 1024) * 1024);
		long t = ((((size * 1024) * 1024) * 1024) * 1024);

		if (lastValue.contains("T")) {
			hrSize = t;

		} else if (lastValue.contains("G")) {
			hrSize = g;

		} else if (lastValue.contains("M")) {

			hrSize = m;

		} else if (lastValue.contains("K")) {

			hrSize = k;

		} else {
			hrSize = b;

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

	public String getThershold() {
		return thershold;
	}

	public void setThershold(String thershold) {
		this.thershold = thershold;
	}

	public String getMetricInstanceId() {
		return metricInstanceId;
	}

	public void setMetricInstanceId(String metricInstanceId) {
		this.metricInstanceId = metricInstanceId;
	}

}
