package linuxoraclehealthcheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
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
public class LinuxHCPSCheckPlugin extends BasePlugin {
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
	int row = 1;

	public LinuxHCPSCheckPlugin() {
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

	private void gettnslsnr() throws IOException {
		try {
			Process p1 = Runtime.getRuntime().exec(new String[] { "ps", "-ef" });
			InputStream input = p1.getInputStream();
			Process p2 = Runtime.getRuntime().exec(new String[] { "grep", "[t]nslsnr" });
			OutputStream output = p2.getOutputStream();
			IOUtils.copy(input, output);
			output.close(); // signals grep to finish
			List<String> lines = IOUtils.readLines(p2.getInputStream());
			String listString = "";

			for (String s : lines) {
				listString += s;
			}
			String[] arr = listString.split("\\s+");
			String line0 = "";
			String line1 = "";
			String line2 = "";
			String line3 = "";
			String line4 = "";
			String line5 = "";
			String line6 = "";
			String line7 = "";
			for (int j = 0; j < arr.length; j++) {

				if (j == 0) {
					line0 = arr[j];
				}
				if (j == 1) {
					line1 = arr[j];
				}
				if (j == 2) {
					line2 = arr[j];
				}
				if (j == 3) {
					line3 = arr[j];
				}
				if (j == 4) {
					line4 = arr[j];
				}
				if (j == 5) {
					line5 = arr[j];
				}
				if (j == 6) {
					line6 = arr[j];
				}
				if (j >= 7) {
					line7 = line7 + " " + arr[j];
				}
			}
			String UID = line0;
			String PID = line1;
			String PPID = line2;
			String cvalue = line3;
			String STIME = line4;
			String TTY = line5;
			String TIME = line6;
			String CMD = line7;

			// UID
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "UID" + "\\\"";
			result = result + " Value=\\\"" + UID + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// PID
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "PID" + "\\\"";
			result = result + " Value=\\\"" + PID + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// PPID
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "PPID" + "\\\"";
			result = result + " Value=\\\"" + PPID + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// C
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "C" + "\\\"";
			result = result + " Value=\\\"" + cvalue + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// STIME
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "STIME" + "\\\"";
			result = result + " Value=\\\"" + STIME + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// TTY
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "TTY" + "\\\"";
			result = result + " Value=\\\"" + TTY + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// TIME
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "TIME" + "\\\"";
			result = result + " Value=\\\"" + TIME + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// CMD
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "CMD" + "\\\"";
			result = result + " Value=\\\"" + CMD + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";
			row++;
		} catch (Exception ex) {

			// System.out.println("Exception In PS");
		}
	}

	private void getohasdbin() throws IOException {
		try {
			Process p1 = Runtime.getRuntime().exec(new String[] { "ps", "-ef" });
			InputStream input = p1.getInputStream();
			Process p2 = Runtime.getRuntime().exec(new String[] { "grep", "[o]hasd.bin" });
			OutputStream output = p2.getOutputStream();
			IOUtils.copy(input, output);
			output.close(); // signals grep to finish
			List<String> lines = IOUtils.readLines(p2.getInputStream());
			String listString = "";

			for (String s : lines) {
				listString += s;
			}

			String[] arr = listString.split("\\s+");
			String line0 = "";
			String line1 = "";
			String line2 = "";
			String line3 = "";
			String line4 = "";
			String line5 = "";
			String line6 = "";
			String line7 = "";
			for (int j = 0; j < arr.length; j++) {

				if (j == 0) {
					line0 = arr[j];
				}
				if (j == 1) {
					line1 = arr[j];
				}
				if (j == 2) {
					line2 = arr[j];
				}
				if (j == 3) {
					line3 = arr[j];
				}
				if (j == 4) {
					line4 = arr[j];
				}
				if (j == 5) {
					line5 = arr[j];
				}
				if (j == 6) {
					line6 = arr[j];
				}
				if (j >= 7) {
					line7 = line7 + " " + arr[j];
				}
			}
			String UID = line0;
			String PID = line1;
			String PPID = line2;
			String cvalue = line3;
			String STIME = line4;
			String TTY = line5;
			String TIME = line6;
			String CMD = line7;

			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "UID" + "\\\"";
			result = result + " Value=\\\"" + UID + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// PID
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "PID" + "\\\"";
			result = result + " Value=\\\"" + PID + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// PPID
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "PPID" + "\\\"";
			result = result + " Value=\\\"" + PPID + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// C
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "C" + "\\\"";
			result = result + " Value=\\\"" + cvalue + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// STIME
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "STIME" + "\\\"";
			result = result + " Value=\\\"" + STIME + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// TTY
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "TTY" + "\\\"";
			result = result + " Value=\\\"" + TTY + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// TIME
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "TIME" + "\\\"";
			result = result + " Value=\\\"" + TIME + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// CMD
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "CMD" + "\\\"";
			result = result + " Value=\\\"" + CMD + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";
			row++;
		} catch (Exception ex) {

			System.out.println("Exception In PS");
		}
	}

	private void getohasdemagent() throws IOException {
		try {
			Process p1 = Runtime.getRuntime().exec(new String[] { "ps", "-ef" });
			InputStream input = p1.getInputStream();
			Process p2 = Runtime.getRuntime().exec(new String[] { "grep", "[b]in/emagent" });
			OutputStream output = p2.getOutputStream();
			IOUtils.copy(input, output);
			output.close(); // signals grep to finish
			List<String> lines = IOUtils.readLines(p2.getInputStream());
			String listString = "";

			for (String s : lines) {
				listString += s;
			}

			String[] arr = listString.split("\\s+");
			String line0 = "";
			String line1 = "";
			String line2 = "";
			String line3 = "";
			String line4 = "";
			String line5 = "";
			String line6 = "";
			String line7 = "";
			for (int j = 0; j < arr.length; j++) {

				if (j == 0) {
					line0 = arr[j];
				}
				if (j == 1) {
					line1 = arr[j];
				}
				if (j == 2) {
					line2 = arr[j];
				}
				if (j == 3) {
					line3 = arr[j];
				}
				if (j == 4) {
					line4 = arr[j];
				}
				if (j == 5) {
					line5 = arr[j];
				}
				if (j == 6) {
					line6 = arr[j];
				}
				if (j >= 7) {
					line7 = line7 + " " + arr[j];
				}
			}
			String UID = line0;
			String PID = line1;
			String PPID = line2;
			String cvalue = line3;
			String STIME = line4;
			String TTY = line5;
			String TIME = line6;
			String CMD = line7;

			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "UID" + "\\\"";
			result = result + " Value=\\\"" + UID + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// PID
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "PID" + "\\\"";
			result = result + " Value=\\\"" + PID + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// PPID
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "PPID" + "\\\"";
			result = result + " Value=\\\"" + PPID + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// C
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "C" + "\\\"";
			result = result + " Value=\\\"" + cvalue + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// STIME
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "STIME" + "\\\"";
			result = result + " Value=\\\"" + STIME + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// TTY
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "TTY" + "\\\"";
			result = result + " Value=\\\"" + TTY + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// TIME
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "TIME" + "\\\"";
			result = result + " Value=\\\"" + TIME + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// CMD
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "CMD" + "\\\"";
			result = result + " Value=\\\"" + CMD + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";
			row++;
		} catch (Exception ex) {

			System.out.println("Exception In PS");
		}
	}

	private void bconsole() throws IOException {
		try {
			Process p1 = Runtime.getRuntime().exec(new String[] { "ps", "-ef" });
			InputStream input = p1.getInputStream();
			Process p2 = Runtime.getRuntime().exec(new String[] { "grep", "[d]bconsole" });
			OutputStream output = p2.getOutputStream();
			IOUtils.copy(input, output);
			output.close(); // signals grep to finish
			List<String> lines = IOUtils.readLines(p2.getInputStream());
			String listString = "";
			// ps -ef|grep [d]bconsole;

			for (String s : lines) {
				listString += s;
			}

			String[] arr = listString.split("\\s+");
			String line0 = "";
			String line1 = "";
			String line2 = "";
			String line3 = "";
			String line4 = "";
			String line5 = "";
			String line6 = "";
			String line7 = "";
			for (int j = 0; j < arr.length; j++) {

				if (j == 0) {
					line0 = arr[j];
				}
				if (j == 1) {
					line1 = arr[j];
				}
				if (j == 2) {
					line2 = arr[j];
				}
				if (j == 3) {
					line3 = arr[j];
				}
				if (j == 4) {
					line4 = arr[j];
				}
				if (j == 5) {
					line5 = arr[j];
				}
				if (j == 6) {
					line6 = arr[j];
				}
				if (j >= 7) {
					line7 = line7 + " " + arr[j];
				}
			}
			String UID = line0;
			String PID = line1;
			String PPID = line2;
			String cvalue = line3;
			String STIME = line4;
			String TTY = line5;
			String TIME = line6;
			String CMD = line7;

			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "UID" + "\\\"";
			result = result + " Value=\\\"" + UID + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// PID
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "PID" + "\\\"";
			result = result + " Value=\\\"" + PID + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// PPID
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "PPID" + "\\\"";
			result = result + " Value=\\\"" + PPID + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// C
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "C" + "\\\"";
			result = result + " Value=\\\"" + cvalue + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// STIME
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "STIME" + "\\\"";
			result = result + " Value=\\\"" + STIME + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// TTY
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "TTY" + "\\\"";
			result = result + " Value=\\\"" + TTY + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// TIME
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "TIME" + "\\\"";
			result = result + " Value=\\\"" + TIME + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";

			// CMD
			result = result + "\\u003cProperties\\u003e";
			result = result + "\\u003cProperty";
			result = result + " Type=\\\"" + "Text" + "\\\"";
			result = result + " Row=\\\"" + row + "\\\"";
			result = result + " Key=\\\"" + "CMD" + "\\\"";
			result = result + " Value=\\\"" + CMD + "\\\"";
			result = result + " /\\u003e";
			result = result + "\\u003c/Properties\\u003e";
			row++;
		} catch (Exception ex) {

			System.out.println("Exception In PS");
		}
	}

	public void updateData(String str_label)
			throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException,
			XPathExpressionException, ParserConfigurationException, SAXException {
		gettnslsnr();
		getohasdbin();
		getohasdemagent();
		bconsole();
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
			postData = postData + " MonitorName=\\\"" + "LinuxHCPSCheckPlugin" + "\\\"";
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
