package linuxoraclehealthcheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
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
 * @PluginDesc:Oracle Health Check On Linux
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :LinuxOracleHealthCheck master Plugin
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018'
 * @Details: Oracle Health Check On Linux Platform.
 */
public class LinuxOracleHealthCheck extends BasePlugin {
	private String name;
	private int sleepInterval;
	private int scheduleType;
	private String ConnectionString;
	private String resultStatus = "";
	private String thershold;
	private String metricInstanceId;
	static Class<?> cls;
	String serverid = "";
	String postData = "";
	String returnstr = "";
	CommonPluginMessages cpm = new CommonPluginMessages();

	@Override
	public void run() {

		try {

			serverid = cpm.serverId();
			while (true) {

				postData = careateClassObjectUsingReflection();

				int timeinterval = 500 * 1000 * 60;

				Thread.sleep(timeinterval);

			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("OracleHealthCheck threw error, full stack trace follows:" + e1);
			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("OracleHealthCheck threw error, full stack trace follows:" + e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("OracleHealthCheck threw error, full stack trace follows:" + e3);
			// e3.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (Exception ex) {
			logger.error("OracleHealthCheck Threw ERROR:" + ex.toString());

		}
	}

	public String careateClassObjectUsingReflection()
			throws IOException, ClassNotFoundException, SQLException, SAXException, ParserConfigurationException {

		String path = "/var/datavail/lib/Datavail.Delta.Agent.LinuxOracleHealthCheck.jar";
		FileClassFinder1 fl = new FileClassFinder1(path);

		String packages = "linuxoraclehealthcheck";

		fl.findCls(packages);
		Method method;

		Class<?> noparam[] = { String.class };

		String PostingId = java.util.UUID.randomUUID().toString();
		String PluginPostData = "";
		String RetPostData = "";
		String str_userEmail = "";
		for (int i = 0; i < fl.findCls("linuxoraclehealthcheck").size(); i++) {
			if (fl.findCls("linuxoraclehealthcheck").get(i).toString().contains("Plugin")) {
				// String RetPostData = "";
				String startSubPostData = "";
				String endSubPostData = "";

				String PostData = "";
				startSubPostData = "";
				endSubPostData = "";

				try {
					cls = Class.forName(packages + "."
							+ fl.findCls("linuxoraclehealthcheck").get(i).toString().replaceAll("class ", ""));

				} catch (ClassNotFoundException e) {

					// e.printStackTrace();
				}

				Object obj = null;

				try {

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

					String newData = eElement.getAttribute("Data");

					String newInstanceName = "";
					String newInstanceNames = "";

					// String str_userEmail="";
					String str_userEmails = "";

					String str_NoOfInstance = "";
					String latest = "";

					int startindex = 0;
					int endindex = 0;

					startindex = newData.lastIndexOf("NoOfInstance");
					// endindex = newData.lastIndexOf("Label");
					endindex = newData.lastIndexOf("Email");

					latest = newData.substring(startindex, endindex);
					latest.trim();
					newInstanceName = latest.replace("NoOfInstance=", "");
					newInstanceNames = newInstanceName.replaceAll("\"", "");
					newInstanceNames.trim();
					str_NoOfInstance = newInstanceNames;
					str_NoOfInstance = str_NoOfInstance.trim();

					// Start - Get Emailid
					startindex = newData.lastIndexOf("Email");
					endindex = newData.lastIndexOf("Label");
					latest = newData.substring(startindex, endindex);
					latest.trim();
					str_userEmail = latest.replace("Email=", "");
					str_userEmails = str_userEmail.replaceAll("\"", "");
					str_userEmails.trim();
					str_userEmail = str_userEmails;
					str_userEmail = str_userEmail.trim();
					// End - Get Emailid

					obj = cls.newInstance();
					int excuteLevel = get(obj, "excuteLevel");

					if (excuteLevel == 1) {

						for (Integer noOfinst = 1; noOfinst <= Integer.parseInt(str_NoOfInstance); noOfinst++) {
							ArrayList<String> inputData = new ArrayList<String>();

							startindex = newData.lastIndexOf("ConnectionString" + noOfinst);
							endindex = newData.lastIndexOf("InstanceName" + noOfinst);

							latest = newData.substring(startindex, endindex);
							latest.trim();
							newInstanceName = latest.replace("ConnectionString" + noOfinst + "=", "");
							newInstanceNames = newInstanceName.replaceAll("\"", "");
							newInstanceNames.trim();
							String connString = newInstanceNames;

							startindex = newData.lastIndexOf("InstanceName" + noOfinst);
							endindex = newData.lastIndexOf("InstanceId" + noOfinst);

							latest = newData.substring(startindex, endindex);
							latest.trim();
							newInstanceName = latest.replace("InstanceName" + noOfinst + "=", "");
							newInstanceNames = newInstanceName.replaceAll("\"", "");
							newInstanceNames.trim();
							String instanceName = newInstanceNames;

							startindex = newData.lastIndexOf("InstanceId" + noOfinst);
							str_NoOfInstance = str_NoOfInstance.trim();

							if (noOfinst.equals(Integer.parseInt(str_NoOfInstance))) {

								endindex = newData.lastIndexOf("NoOfInstance");
							} else {

								endindex = newData.lastIndexOf("ConnectionString" + (noOfinst + 1));
							}
							latest = newData.substring(startindex, endindex);
							latest.trim();
							newInstanceName = latest.replace("InstanceId" + noOfinst + "=", "");
							newInstanceNames = newInstanceName.replaceAll("\"", "");
							newInstanceNames.trim();
							String instanceId = newInstanceNames;

							inputData.add("connString=" + connString);
							inputData.add("instanceName=" + instanceName);
							inputData.add("instanceId=" + instanceId);

							method = cls.getDeclaredMethod("makedata", noparam);
							returnstr = (String) method.invoke(obj, inputData.toString());

							startSubPostData = startSubPostData + "\\u003cMonitorOutput";
							startSubPostData = startSubPostData + "\\u003e";

							endSubPostData = endSubPostData + "\\u003c/MonitorOutput";
							endSubPostData = endSubPostData + "\\u003e";

							PostData = PostData + startSubPostData + returnstr + endSubPostData;
							PluginPostData = PluginPostData + PostData;
							// RetPostData = "{\"Data\"" + ":" +
							// "\"\\u003cHealthCheck";
							// RetPostData = RetPostData + " PostingID=\\\"" +
							// PostingId + "\\\"";
							// RetPostData = RetPostData + " Email=\\\"" +
							// str_userEmail + "\\\"" + "\\u003e";

							// RetPostData = RetPostData + PostData;
							// RetPostData = RetPostData + "\\u003c/HealthCheck"
							// + "\\u003e\"";
							// RetPostData = RetPostData + ",";

							// String hostname =
							// cpm.RunLinuxCommand("hostname");

							// InetAddress addr = InetAddress.getLocalHost();
							// String IpAddress = addr.getHostAddress();

							// RetPostData = RetPostData + " \"Hostname\"" +
							// ":\"" + hostname + "\"," + "\"IpAddress\""
							// + ":\"" + IpAddress + "\",";
							// RetPostData = RetPostData + " \"ServerId\"" +
							// ":\"" + serverid + "\"," + "\"TenantId\""
							// + ":" +
							// "\"1a19a18a-846c-49da-93c1-8948afdc0151\"";
							// RetPostData = RetPostData + "," + "\"Timestamp\""
							// + ":\"" + "\\/Date("
							// + System.currentTimeMillis() + ")\\/" + "\"";
							// RetPostData = RetPostData + "," + "\"Id\":null" +
							// "," + "\"PopReceipt\":0" + ","
							// + "\"DequeueCount\":0}";
							// String
							// strClassName=fl.findCls("linuxoraclehealthcheck").get(i).toString();
							// .info("OracleHealthCheck "+strClassName+"
							// DATA sent: " + RetPostData);
							// WebResource webResource = cpm.getURL("Linux
							// Oracle: Health Check");

							// ClientResponse response =
							// webResource.path("/{id}").queryParam("id",
							// serverid)
							// .type("application/json").post(ClientResponse.class,
							// RetPostData);
							// logger.info("OracleHealthCheck "+strClassName+"
							// POST response: " + response);

						}

					} else {
						method = cls.getDeclaredMethod("makedata", noparam);
						returnstr = (String) method.invoke(obj, "");

						startSubPostData = startSubPostData + "\\u003cMonitorOutput";
						startSubPostData = startSubPostData + "\\u003e";

						endSubPostData = endSubPostData + "\\u003c/MonitorOutput";
						endSubPostData = endSubPostData + "\\u003e";

						PostData = PostData + startSubPostData + returnstr + endSubPostData;
						PluginPostData = PluginPostData + PostData;
						// RetPostData = "{\"Data\"" + ":" +
						// "\"\\u003cHealthCheck";
						// RetPostData = RetPostData + " PostingID=\\\"" +
						// PostingId + "\\\"";
						// RetPostData = RetPostData + " Email=\\\"" +
						// str_userEmail + "\\\"" + "\\u003e";

						// RetPostData = RetPostData + PostData;
						// RetPostData = RetPostData + "\\u003c/HealthCheck" +
						// "\\u003e\"";
						// RetPostData = RetPostData + ",";

						// String hostname = cpm.RunLinuxCommand("hostname");

						// InetAddress addr = InetAddress.getLocalHost();
						// String IpAddress = addr.getHostAddress();

						// RetPostData = RetPostData + " \"Hostname\"" + ":\"" +
						// hostname + "\"," + "\"IpAddress\"" + ":\""
						// + IpAddress + "\",";
						// RetPostData = RetPostData + " \"ServerId\"" + ":\"" +
						// serverid + "\"," + "\"TenantId\"" + ":"
						// + "\"1a19a18a-846c-49da-93c1-8948afdc0151\"";
						// RetPostData = RetPostData + "," + "\"Timestamp\"" +
						// ":\"" + "\\/Date("
						// + System.currentTimeMillis() + ")\\/" + "\"";
						// RetPostData = RetPostData + "," + "\"Id\":null" + ","
						// + "\"PopReceipt\":0" + ","
						// + "\"DequeueCount\":0}";
						// String
						// strClassName=fl.findCls("linuxoraclehealthcheck").get(i).toString();
						// logger.info("OracleHealthCheck "+strClassName+" DATA
						// sent: " + RetPostData);
						// WebResource webResource = cpm.getURL("Linux Oracle:
						// Health Check");

						// ClientResponse response =
						// webResource.path("/{id}").queryParam("id", serverid)
						// .type("application/json").post(ClientResponse.class,
						// RetPostData);
						// logger.info("OracleHealthCheck "+strClassName+" POST
						// response: " + response);

					}

				} catch (Exception e) {

					// e.printStackTrace();
				} finally {
					if (obj != null) {
						obj = null;
					}
				}

			}
		}
		RetPostData = "{\"Data\"" + ":" + "\"\\u003cHealthCheck";
		RetPostData = RetPostData + " PostingID=\\\"" + PostingId + "\\\"";
		RetPostData = RetPostData + " Email=\\\"" + str_userEmail + "\\\"" + "\\u003e";

		RetPostData = RetPostData + PluginPostData;
		RetPostData = RetPostData + "\\u003c/HealthCheck" + "\\u003e\"";
		RetPostData = RetPostData + ",";
		String hostname = cpm.RunLinuxCommand("hostname");

		InetAddress addr = InetAddress.getLocalHost();
		String IpAddress = addr.getHostAddress();

		RetPostData = RetPostData + " \"Hostname\"" + ":\"" + hostname + "\"," + "\"IpAddress\"" + ":\"" + IpAddress
				+ "\",";
		RetPostData = RetPostData + " \"ServerId\"" + ":\"" + serverid + "\"," + "\"TenantId\"" + ":"
				+ "\"1a19a18a-846c-49da-93c1-8948afdc0151\"";
		RetPostData = RetPostData + "," + "\"Timestamp\"" + ":\"" + "\\/Date(" + System.currentTimeMillis() + ")\\/"
				+ "\"";
		RetPostData = RetPostData + "," + "\"Id\":null" + "," + "\"PopReceipt\":0" + "," + "\"DequeueCount\":0}";
		// String
		// strClassName=fl.findCls("linuxoraclehealthcheck").get(i).toString();
		logger.info("OracleHealthCheck " + " DATA sent: " + RetPostData);
		WebResource webResource = cpm.getURL("Linux Oracle: Health Check");
		try {
			ClientResponse response = webResource.path("/{id}").queryParam("id", serverid).type("application/json")
					.post(ClientResponse.class, RetPostData);
			logger.info("OracleHealthCheck " + " POST response: " + response);
		} catch (Exception ex) {
			logger.error("OracleHealthCheck POST ERROR:" + ex.toString());
		}

		return "";
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

	@SuppressWarnings("unchecked")
	public static <V> V get(Object object, String fieldName) {
		Class<?> clazz = object.getClass();
		while (clazz != null) {
			try {
				Field field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				return (V) field.get(object);
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return null;
	}

}
