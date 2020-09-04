package logwatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import pluginmessages.CommonPluginMessages;
/**
 * @File_Desc: Log watcher info Plugin  Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :LogWatcher
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class LogWatcher extends BasePlugin {
	private String name;
	private int sleepInterval;
	private String ConnectionString;

	private int scheduleType;
	private String data = "";
	private String thershold;
	private long lineNumber = 0;
	int startindex;
	int endindex;
	String postData = "";
	String serverID = "";
	String status = "";
	private String metricInstanceId;
	boolean boolflag = false;

	@Override
	public void run() {
		try {
			CommonPluginMessages cpm = new CommonPluginMessages();

			serverID = cpm.serverId();
			while (true) {
				updateData();

				int timeinterval = cpm.buildSchedule(scheduleType, sleepInterval);
				if (timeinterval != 0) {
					Thread.sleep(timeinterval);
				}

			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("ErrorLog threw error, full stack trace follows:" + e1);

			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("ErrorLog threw error, full stack trace follows:" + e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("ErrorLog threw error, full stack trace follows:" + e3);
			// e3.printStackTrace();
		}

		catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (Exception ex) {
			logger.error("ErrorLog Threw ERROR:" + ex.toString());

		}

	}

	public void updateData() throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException,
			ParserConfigurationException, SAXException, XPathExpressionException {

		File configfile = new File("config.xml");

		FileInputStream fileInput = new FileInputStream(configfile);

		Properties properties = new Properties();
		properties.loadFromXML(fileInput);
		String DeltaAgent_file = properties.getProperty("deltaagent");
		// String logWatcherReadlinePath =
		// properties.getProperty("FileNumberPath");
		File fXmlFile = new File(DeltaAgent_file);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		String xPathString = "/AgentConfiguration/MetricInstance[@Id='" + metricInstanceId + "']";
		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(xPathString);
		Node nNode = (Node) xPathExpression.evaluate(doc, XPathConstants.NODE);

		Element eElement = (Element) nNode;

		String newData = eElement.getAttribute("Data");

		/////////// 1st part
		String newFileWatcherString = "";
		String newFileWatcherStrings = "";

		startindex = 0;
		endindex = 0;

		startindex = newData.lastIndexOf("FileNameToWatch");
		endindex = newData.lastIndexOf("Label");
		String latest = newData.substring(startindex, endindex);
		latest.trim();
		newFileWatcherString = latest.replace("FileNameToWatch=", "");
		newFileWatcherStrings = newFileWatcherString.replaceAll("\"", "");
		newFileWatcherStrings = newFileWatcherStrings.trim();

		String strfilename = newFileWatcherStrings.substring(newFileWatcherStrings.lastIndexOf("/") + 1);
		String logWatcherReadlinePath = "num_" + strfilename;
		String newMatchExpressionString = "";
		String newMatchExpressionStrings = "";

		int startindexs = 0;
		int endindexs = 0;

		startindexs = newData.indexOf("MatchExpressions");

		endindexs = newData.lastIndexOf("/MatchExpressions");

		latest = newData.substring(startindexs, endindexs);
		latest.trim();
		newMatchExpressionString = latest.replace(">", "");
		newMatchExpressionString = newMatchExpressionString.replace("<MatchExpression expression=", "");
		newMatchExpressionString = newMatchExpressionString.replace("/>", ",");
		newMatchExpressionString = newMatchExpressionString.replace("<", "");
		newMatchExpressionString = newMatchExpressionString.replace("/", ",");
		newMatchExpressionString = newMatchExpressionString.replace("MatchExpressions", "");
		newMatchExpressionStrings = newMatchExpressionString.replaceAll("\"", "");
		// newMatchExpressionStrings =
		// newMatchExpressionString.replaceAll("\\s", "");
		newMatchExpressionStrings = newMatchExpressionStrings.replaceAll("\\|", ";");
		newMatchExpressionStrings = newMatchExpressionStrings.trim();

		String[] strArr = newMatchExpressionStrings.split(",");

		ArrayList<String> al = new ArrayList<>();
		for (String s : strArr) {

			String[] str = s.split(";");

			Collections.addAll(al, str);

		}

		String newExcludeExpressionString = "";
		String newExcludeExpressionStrings = "";
		startindexs = newData.indexOf("ExcludeExpressions");

		endindexs = newData.lastIndexOf("/ExcludeExpressions");
		if (endindexs != -1) {

			latest = newData.substring(startindexs, endindexs);
			latest.trim();
			newExcludeExpressionString = latest.replace(">", "");
			newExcludeExpressionString = newExcludeExpressionString.replace("<ExcludeExpression expression=", "");
			newExcludeExpressionString = newExcludeExpressionString.replace("/>", ",");
			newExcludeExpressionString = newExcludeExpressionString.replace("<", "");
			newExcludeExpressionString = newExcludeExpressionString.replace("/", ",");
			newExcludeExpressionString = newExcludeExpressionString.replace("ExcludeExpressions", "");
			newExcludeExpressionStrings = newExcludeExpressionString.replaceAll("\"", "");
			newExcludeExpressionStrings = newExcludeExpressionStrings.replaceAll("\\|", ";");
			newExcludeExpressionStrings = newExcludeExpressionStrings.trim();
		}

		String[] strArrs = newExcludeExpressionStrings.split(",");
		ArrayList<String> excludeAl = new ArrayList<>();
		for (String s : strArrs) {

			String[] str = s.split(";");
			Collections.addAll(excludeAl, str);

		}

		////// File Reading starts here ///////

		try {
			final JumpToLine jt2 = new JumpToLine(new File(logWatcherReadlinePath));

			jt2.seek();
			while (jt2.hasNext()) {
				String line = jt2.readLine();
				lineNumber = Long.parseLong(line);
			}

			jt2.close();
			boolflag = false;
		} catch (Exception e) {
			lineNumber = 1;
			boolflag = true;
		} finally {
			// Close the underlying reader and LineIterator.

		}
		try {
			final JumpToLine jtl = new JumpToLine(new File(newFileWatcherStrings));
			try {

				jtl.seek(lineNumber);
				// While there are any lines after and including line 10,
				// read them.
				while (jtl.hasNext()) {

					final String line = jtl.readLine();
					// lineNumber = jtl.getLastLineRead();
					lineNumber++;

					for (int i1 = 0; i1 < al.size(); i1++) {

						if (line.contains(al.get(i1).trim())) {
							boolean boolfound = false;
							if (!newExcludeExpressionStrings.isEmpty()) {
								for (int i2 = 0; i2 < excludeAl.size(); i2++) {
									if (line.contains(excludeAl.get(i2).trim())) {
										boolfound = true;

									}

								}
							}

							if (!boolfound) {
								// if (boolflag == false)"" {
								
								System.out.println("Inside ");
								data = line;
								data = data.replaceAll("'", "\\\\'");
								data = data.replaceAll("\"", "\\\\\"");
								CommonPluginMessages cpm = new CommonPluginMessages();
								WebResource webResource = cpm.getURL("Linux: Log Watcher Plugin");

								String postData = getPostData();
								logger.info("LogWatcher DATA sent: " + postData);

								try {
									ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
											.type("application/json").post(ClientResponse.class, postData);
									logger.info("LogWatcher POST response:" + response);
								} catch (Exception ex) {

									logger.error("LogWatcher POST ERROR:" + ex.toString());
								}
							}
						}
					}
				}
				// }

				PrintWriter writer = new PrintWriter(logWatcherReadlinePath, "UTF-8");
				writer.println(lineNumber);
				writer.close();
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				pw.flush();
				String stackTrace = sw.toString();
				int lenoferrorstr = stackTrace.length();
				if (lenoferrorstr > 200) {
					status = "Error:" + stackTrace.substring(0, 200);
				} else {
					status = "Error:" + stackTrace.substring(0, lenoferrorstr - 1);

				}
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			int lenoferrorstr = stackTrace.length();
			if (lenoferrorstr > 200) {
				status = "Error:" + stackTrace.substring(0, 200);
			} else {
				status = "Error:" + stackTrace.substring(0, lenoferrorstr - 1);

			}
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
		// String str_instanceName "";
		// Instance Name Start

		String newData = eElement.getAttribute("Data");

		String newInstanceName = "";
		String newInstanceNames = "";
		String str_instanceName = "";
		int startindex = 0;
		int endindex = 0;

		startindex = newData.lastIndexOf("FileNameToWatch");
		endindex = newData.lastIndexOf("InstanceName");
		String latest = newData.substring(startindex, endindex);
		latest.trim();
		newInstanceName = latest.replace("InstanceName=", "");
		newInstanceNames = newInstanceName.replaceAll("\"", "");
		newInstanceNames.trim();
		str_instanceName = newInstanceNames;

		// Instance Name Stop

		String str_product = eElement.getAttribute("product");

		CommonPluginMessages cm = new CommonPluginMessages();
		String hostname = cm.RunLinuxCommand("hostname");

		InetAddress addr = InetAddress.getLocalHost();
		String IpAddress = addr.getHostAddress();

		System.currentTimeMillis();
		if (status.contains("Error:")) {
			// to do check agent error status
			String errorPostData = cm.buildErrorString("LogWatcherPlugin", "PostData", status, serverID,
					str_metricInstanceId, formattedTime, str_product, IpAddress);

			if (!errorPostData.isEmpty()) {
				logger.info("AgentError DATA sent: " + errorPostData);

				MysqlConnection oracleconn = new MysqlConnection();

				WebResource webResource = oracleconn.client();

				ClientResponse response = webResource.path("/{id}").queryParam("id", serverID).type("application/json")
						.post(ClientResponse.class, errorPostData);
				logger.info("AgentError POST response: " + response);
			}

		} else {
			postData = "{\"Data\"" + ":" + "\"\\u003cLogWatcherPluginOutput  ";
			postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
			postData = postData + " product=\\\"" + str_product + "\\\"";
			postData = postData + " productVersion=\\\"" + "" + "\\\"";
			postData = postData + " productLevel=\\\"\\\"";
			postData = postData + " productEdition=\\\"" + "" + "\\\"";
			postData = postData + " metricInstanceId=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " label=\\\"" + str_label + "\\\"";
			postData = postData + " matchingLine=\\\"" + data + "\\\"";
			postData = postData + " instanceName=\\\"" + str_instanceName + "\\\"";
			postData = postData + " instanceStatus=\\\"" + "Up" + "\\\" /\\u003e\"" + ",";
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
	 * @return the lineNumber
	 */
	public long getLineNumber() {
		return lineNumber;
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
