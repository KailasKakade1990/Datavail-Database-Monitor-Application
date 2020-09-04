package linuxoraclehealthcheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

import pluginmessages.CommonPluginMessages;

/**
 * @PluginDesc:Oracle Health Check On Linux
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :LinuxRamPlugin
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 */
public class LinuxRamPlugin extends BasePlugin {
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
	public int excuteLevel;

	public LinuxRamPlugin() {
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

		String xPathString = "/AgentConfiguration/MetricInstance[@AdapterClass='LinuxOracleHealthCheck']";

		XPath xPath = XPathFactory.newInstance().newXPath();

		XPathExpression xPathExpression = xPath.compile(xPathString);
		Node nNode = (Node) xPathExpression.evaluate(doc, XPathConstants.NODE);

		Element eElement = (Element) nNode;

		String str_metricInstanceId = eElement.getAttribute("Id");

		System.currentTimeMillis();
		CommonPluginMessages cpm = new CommonPluginMessages();

		String cmd = "free | grep  Mem";
		String postData = "";
		String memory = cpm.RunLinuxGrepCommand(cmd);

		if (memory.contains("Error:")) {
			// to do check agent error status
			/*
			 * String errorPostData = cpm.buildErrorString("LinuxRamPlugin",
			 * "PostData", memory, ServerId, str_metricInstanceId,
			 * formattedTime, str_product, IpAddress);
			 * 
			 * if (!errorPostData.isEmpty()) {
			 * logger.info("AgentError DATA sent: " + errorPostData);
			 * 
			 * String serverID = cpm.serverId();
			 * 
			 * MysqlConnection oracleconn = new MysqlConnection();
			 * 
			 * WebResource webResource = oracleconn.client();
			 * 
			 * ClientResponse response =
			 * webResource.path("/{id}").queryParam("id",
			 * serverID).type("application/json") .post(ClientResponse.class,
			 * errorPostData); logger.info("AgentError POST response: " +
			 * response); }
			 */

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

			postData = "\\u003cMonitor ";
			postData = postData + " DBInstanceID=\\\"" + "" + "\\\"";
			postData = postData + " DatabaseID=\\\"" + "" + "\\\"";
			postData = postData + " AgentVersion=\\\"" + "6.0.0.0" + "\\\"";
			postData = postData + " DateTime=\\\"" + formattedTime + "\\\"";
			postData = postData + " MonitorInstanceID=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " MonitorName=\\\"" + "MemoryHealthCheckPlugin" + "\\\"";
			postData = postData + " TimeZone=\\\"" + "India Standard Time" + "\\\"";
			postData = postData + " Culture=\\\"" + "en-IN" + "\\\"";

			postData = postData + "\\u003e";

			postData = postData + "\\u003cProperties\\u003e";

			postData = postData + "\\u003cProperty";
			postData = postData + " Type=\\\"" + "Text" + "\\\"";
			postData = postData + " Row=\\\"" + "1" + "\\\"";
			postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
			postData = postData + " Value=\\\"" + "OS Free Physical Memory(GB)" + "\\\"";
			postData = postData + "/\\u003e";
			postData = postData + "\\u003c/Properties\\u003e";

			postData = postData + "\\u003cProperties\\u003e";
			postData = postData + "\\u003cProperty";
			postData = postData + " Type=\\\"" + "Text" + "\\\"";
			postData = postData + " Row=\\\"" + "1" + "\\\"";
			postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
			postData = postData + " Value=\\\"" + _availablePhysicalMemoryBytesFriendly + "\\\"";
			postData = postData + "/\\u003e";
			postData = postData + "\\u003c/Properties\\u003e";

			postData = postData + "\\u003cProperties\\u003e";
			postData = postData + "\\u003cProperty";
			postData = postData + " Type=\\\"" + "Text" + "\\\"";
			postData = postData + " Row=\\\"" + "2" + "\\\"";
			postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
			postData = postData + " Value=\\\"" + "OS Total Physical Memory (GB)" + "\\\"";
			postData = postData + "/\\u003e";
			postData = postData + "\\u003c/Properties\\u003e";

			postData = postData + "\\u003cProperties\\u003e";
			postData = postData + "\\u003cProperty";
			postData = postData + " Type=\\\"" + "Text" + "\\\"";
			postData = postData + " Row=\\\"" + "2" + "\\\"";
			postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
			postData = postData + " Value=\\\"" + _totalPhysicalMemoryBytesFriendly + "\\\"";
			postData = postData + "/\\u003e";
			postData = postData + "\\u003c/Properties\\u003e";

			postData = postData + "\\u003cProperties\\u003e";
			postData = postData + "\\u003cProperty";
			postData = postData + " Type=\\\"" + "Text" + "\\\"";
			postData = postData + " Row=\\\"" + "3" + "\\\"";
			postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
			postData = postData + " Value=\\\"" + "OS Total Virtual Memory Size(GB)" + "\\\"";
			postData = postData + "/\\u003e";
			postData = postData + "\\u003c/Properties\\u003e";

			postData = postData + "\\u003cProperties\\u003e";
			postData = postData + "\\u003cProperty";
			postData = postData + " Type=\\\"" + "Text" + "\\\"";
			postData = postData + " Row=\\\"" + "3" + "\\\"";
			postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
			postData = postData + " Value=\\\"" + _totalVirtualMemoryBytesFriendly + "\\\"";
			postData = postData + "/\\u003e";
			postData = postData + "\\u003c/Properties\\u003e";

			postData = postData + "\\u003c/Monitor";
			postData = postData + "\\u003e";

		}
		return postData;
	}

	public static String convertSize(long size) {
		String hrSize = null;

		/*
		 * double b = size; double k = size / 1024.0; double m = ((size /
		 * 1024.0) / 1024.0); double t = ((((size / 1024.0) / 1024.0) / 1024.0)
		 * / 1024.0);
		 */
		double g = (((size / 1024.0) / 1024.0) / 1024.0);

		DecimalFormat dec = new DecimalFormat("0.000000");
		hrSize = dec.format(g);
		/*
		 * if (t > 1) { hrSize = dec.format(t).concat(" TB"); } else if (g > 1)
		 * { hrSize = dec.format(g).concat(" GB"); } else if (m > 1) { hrSize =
		 * dec.format(m).concat(" MB"); } else if (k > 1) { hrSize =
		 * dec.format(k).concat(" KB"); } else { hrSize =
		 * dec.format(b).concat(" Bytes"); }
		 */

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
