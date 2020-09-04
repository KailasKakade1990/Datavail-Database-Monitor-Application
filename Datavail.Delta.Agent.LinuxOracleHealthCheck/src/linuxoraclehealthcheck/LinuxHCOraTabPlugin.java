package linuxoraclehealthcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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

import pluginmessages.CommonPluginMessages;
/**
 * @PluginDesc:Oracle Health Check On Linux
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :Linux <> Plugin
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 */
public class LinuxHCOraTabPlugin extends BasePlugin {
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
	public int excuteLevel;

	public LinuxHCOraTabPlugin() {
		excuteLevel = 0;
	}

	@Override
	public void run() {

	}

	public String makedata(String str) throws IOException, ParserConfigurationException, SAXException,
			XPathExpressionException, ClassNotFoundException, SQLException {

		str = getPostData();
		return (str);
	}

	public void updateData(String str_label)
			throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException,
			XPathExpressionException, ParserConfigurationException, SAXException {
		try {
			result = "";
			String[] group;
			String line;
			Process p;
			BufferedReader input;

			String groupsFilePath = "/etc/oratab";

			FileReader fileReader = new FileReader(groupsFilePath);
			BufferedReader groupsReader = new BufferedReader(fileReader);
			int row = 1;
			while (groupsReader.ready()) {
				line = groupsReader.readLine();

				if (line.contains("#") || line.trim().equals("") || line.trim().equals("\n")) {
					continue;

				}
				String[] data = line.split(":");

				result = result + "\\u003cProperties\\u003e";
				result = result + "\\u003cProperty";
				result = result + " Type=\\\"" + "Text" + "\\\"";
				result = result + " Row=\\\"" + row + "\\\"";
				result = result + " Key=\\\"" + "ORACLE_SID" + "\\\"";
				result = result + " Value=\\\"" + data[0].trim() + "\\\"";
				result = result + " /\\u003e";
				result = result + "\\u003c/Properties\\u003e";

				result = result + "\\u003cProperties\\u003e";
				result = result + "\\u003cProperty";
				result = result + " Type=\\\"" + "Text" + "\\\"";
				result = result + " Row=\\\"" + row + "\\\"";
				result = result + " Key=\\\"" + "ORACLE_HOME" + "\\\"";
				result = result + " Value=\\\"" + data[1].trim() + "\\\"";
				result = result + " /\\u003e";
				result = result + "\\u003c/Properties\\u003e";

				result = result + "\\u003cProperties\\u003e";
				result = result + "\\u003cProperty";
				result = result + " Type=\\\"" + "Text" + "\\\"";
				result = result + " Row=\\\"" + row + "\\\"";
				result = result + " Key=\\\"" + "Status" + "\\\"";
				result = result + " Value=\\\"" + data[2].trim() + "\\\"";
				result = result + " /\\u003e";
				result = result + "\\u003c/Properties\\u003e";

				row++;
			}

			groupsReader.close();
			fileReader.close();

		} catch (Exception ex) {
			System.out.println("result error: in /etc/oratab");
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

		String xPathString = "/AgentConfiguration/MetricInstance[@AdapterClass='LinuxOracleHealthCheck']";

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

			postData = "\\u003cMonitor ";

			postData = postData + " DBInstanceID=\\\"" + "" + "\\\"";
			postData = postData + " DatabaseID=\\\"" + "" + "\\\"";
			postData = postData + " AgentVersion=\\\"" + "6.0.0.0" + "\\\"";
			postData = postData + " DateTime=\\\"" + formattedTime + "\\\"";
			postData = postData + " MonitorInstanceID=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " MonitorName=\\\"" + "LinuxHCOraTabPlugin" + "\\\"";
			postData = postData + " TimeZone=\\\"" + "India Standard Time" + "\\\"";
			postData = postData + " Culture=\\\"" + "en-IN" + "\\\"";

			postData = postData + "\\u003e";

			postData = postData + result;

			postData = postData + "\\u003c/Monitor";
			postData = postData + "\\u003e";
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

	/**
	 * @return the excuteLevel
	 */
	public int getExcuteLevel() {
		return excuteLevel;
	}

	/**
	 * @param excuteLevel
	 *            the excuteLevel to set
	 */
	public void setExcuteLevels(int excuteLevel) {
		this.excuteLevel = excuteLevel;
	}
}
