package pluginmessages;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.spec.KeySpec;
import java.sql.SQLException;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
/**
 * @File_Desc:Agent Common Code Plugin Class
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :CommonPluginMessages
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 * 
 */
public class CommonPluginMessages {

	String Timestamp = "", Version = "", Edition = "", Header_Message = "";
	String hostname = "", serverid = "", Footer_Message = "";
	String CheckIn_Message = "";
	String agentVersion = "";

	public String checkinmessage(String adapterclass) throws ClassNotFoundException, SQLException {

		try {

			File configfile = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(configfile);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			String DeltaAgent_file = properties.getProperty("deltaagent");
			agentVersion = properties.getProperty("deltaagentversion");

			File fXmlFile = new File(DeltaAgent_file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			String xPathString = "/AgentConfiguration/MetricInstance[@AdapterClass='" + adapterclass + "']";

			XPath xPath = XPathFactory.newInstance().newXPath();
			XPathExpression xPathExpression = xPath.compile(xPathString);

			Node nNode = (Node) xPathExpression.evaluate(doc, XPathConstants.NODE);

			Element eElement = (Element) nNode;

			String str_Data = eElement.getAttribute("Data");

			String str_metricInstanceId = eElement.getAttribute("Id");

			String str_label = eElement.getAttribute("Label");

			CheckIn_Message = "MetricInstanceId:" + " " + str_metricInstanceId + " " + "Label:" + " " + str_label + " "
					+ "(Data:" + " " + str_Data + ")";

		}

		catch (Exception e) {

		}

		return CheckIn_Message;

	}

	public String getVersionNumber() throws IOException {

		try {

			File configfile = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(configfile);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);

			agentVersion = properties.getProperty("deltaagentversion");
		}

		catch (Exception e) {

		}

		return agentVersion;

	}

	// Connection String encript

	public String encrypt(String inputText) throws Exception {

		byte[] _key = { 123, (byte) 217, 19, 11, 24, 26, 85, 45 };
		byte[] _vector = { (byte) 146, 64, (byte) 191, 111, 23, 3, 113, 119 };

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			KeySpec keySpec = new DESKeySpec(_key);
			SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
			IvParameterSpec iv = new IvParameterSpec(_vector);
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			bout.write(cipher.doFinal(inputText.getBytes("ASCII")));
		} catch (Exception e) {

		}
		return new String(Base64.encodeBase64(bout.toByteArray()), "ASCII");
	}

	// Connection String decrypt
	public String decrypt(String inputText) throws Exception {

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] _key = { 123, (byte) 217, 19, 11, 24, 26, 85, 45 };
		byte[] _vector = { (byte) 146, 64, (byte) 191, 111, 23, 3, 113, 119 };

		try {
			KeySpec keySpec = new DESKeySpec(_key);
			SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);
			IvParameterSpec iv = new IvParameterSpec(_vector);

			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, iv);

			byte[] decoded = Base64.decodeBase64(inputText.getBytes("ASCII"));
			bout.write(cipher.doFinal(decoded));
		} catch (Exception e) {

		}
		return new String(bout.toByteArray(), "ASCII");
	}

	public String RunLinuxGrepCommand(String command) {
		String line = null;
		String strstatus = "";
		try {

			String[] cmd = { "/bin/sh", "-c", command };
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = in.readLine()) != null) {
				strstatus = line;
			}
			in.close();
		} catch (Exception e) {

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			int lenoferrorstr = stackTrace.length();
			if (lenoferrorstr > 500) {
				strstatus = "Error:" + stackTrace.substring(0, 500);
			} else {
				strstatus = "Error:" + stackTrace.substring(0, lenoferrorstr - 1);

			}
		}
		return strstatus;

	}

	public WebResource getURL(String adapterClass)
			throws ClassNotFoundException, SQLException, SAXException, ParserConfigurationException {
		String client_url = "";
		String struriaddress = "";
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			client_url = properties.getProperty("clienturl");
			String DeltaAgent_file = properties.getProperty("deltaagent");
			File fXmlFile = new File(DeltaAgent_file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			String xPathString = "";
			String postData = "";

			try {
				xPathString = "/AgentConfiguration/APIURIS/APIURI[@PlugInName='" + adapterClass + "']";
				XPath xPath = XPathFactory.newInstance().newXPath();

				XPathExpression xPathExpression = xPath.compile(xPathString);
				Node nNode = (Node) xPathExpression.evaluate(doc, XPathConstants.NODE);

				Element eElement = (Element) nNode;

				struriaddress = eElement.getAttribute("URIAddress") + "Server/PostInventoryData";

			} catch (Exception e) {
			}
			if (!xPathString.isEmpty()) {
			}

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		Client client = Client.create();

		if (struriaddress == "") {
			struriaddress = client_url;
		}
		WebResource webResource = client.resource(struriaddress);

		return webResource;
	}

	public String buildErrorString(String objectName, String methodName, String errorMessage, String serverId,
			String str_metricInstanceId, String formattedTime, String str_product, String IpAddress)
			throws SAXException, IOException, ParserConfigurationException {

		File configfile = new File("config.xml");
		FileInputStream fileInput = new FileInputStream(configfile);
		Properties properties = new Properties();
		properties.loadFromXML(fileInput);
		String DeltaAgent_file = properties.getProperty("deltaagent");
		agentVersion = properties.getProperty("deltaagentversion");

		File fXmlFile = new File(DeltaAgent_file);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		String xPathString = "";
		String postData = "";
		try {
			xPathString = "/AgentConfiguration/AGENTERRORS/AgentError[@AgentErrorStatus='Enabled']";

		} catch (Exception e) {
		}
		if (!xPathString.isEmpty()) {

			postData = "{\"Data\"" + ":" + "\"\\u003cAgentErrorOutput ";
			postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
			postData = postData + " product=\\\"" + str_product + "\\\"";
			postData = postData + " productVersion=\\\"" + "" + "\\\"";
			postData = postData + " productLevel=\\\"\\\"";
			postData = postData + " productEdition=\\\"" + "" + "\\\"";
			postData = postData + " metricInstanceId=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " ObjectName=\\\"" + objectName + "\\\"";
			postData = postData + " MethodName=\\\"" + methodName + "\\\"";

			postData = postData + " ErrorMessage=\\\"" + errorMessage + "\\\" /\\u003e\"" + ",";

			postData = postData + " \"Hostname\"" + ":\"" + hostname + "\"," + "\"IpAddress\"" + ":\"" + IpAddress
					+ "\",";
			postData = postData + " \"ServerId\"" + ":\"" + serverId + "\"," + "\"TenantId\"" + ":"
					+ "\"1a19a18a-846c-49da-93c1-8948afdc0151\"";
			postData = postData + "," + "\"Timestamp\"" + ":\"" + "\\/Date(" + System.currentTimeMillis() + ")\\/"
					+ "\"";
			postData = postData + "," + "\"Id\":null" + "," + "\"PopReceipt\":0" + "," + "\"DequeueCount\":0}";
		}
		return postData;

	}

	public String RunLinuxCommand(String cmd) throws IOException {

		String s = "";
		Process p = Runtime.getRuntime().exec(cmd);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

		try {
			while ((s = stdInput.readLine()) != null) {

				return s;
			}
			while ((s = stdError.readLine()) != null) {
				return "";
			}
		} catch (Exception e) {
			return "";
		}

		return s;
	}

	public int buildSchedule(int intScheduleType, int intsleepInterval) {

		int schedule = 0;
		switch (intScheduleType) {
		case -1: // RunOnce
		{
			schedule = 0;
			break;
		}
		case 0: // Seconds
		{
			schedule = intsleepInterval * 1000;
			break;
		}
		case 1: // Minute
		{

			schedule = intsleepInterval * 1000 * 60;
			break;
		}
		case 2: // Hours
		{
			schedule = intsleepInterval * 1000 * 60 * 60;
			break;
		}

		default: {
			schedule = 0;
		}
		}

		return schedule;
	}

	/**
	 * @return the agentVersion
	 */
	public String getAgentVersion() {
		return agentVersion;
	}

	/**
	 * @param agentVersion
	 *            the agentVersion to set
	 */
	public void setAgentVersion(String agentVersion) {
		this.agentVersion = agentVersion;
	}

	public String serverId() throws ClassNotFoundException, SQLException, IOException {
		String ServerId = "";

		ServerId = RunLinuxCommand("cat /sys/class/dmi/id/product_uuid");

		if (ServerId == "") {
			ServerId = RunLinuxCommand("hostid");
		} else {
			ServerId = ServerId.replaceAll("UUID:", "");
		}

		return ServerId;

	}

}
