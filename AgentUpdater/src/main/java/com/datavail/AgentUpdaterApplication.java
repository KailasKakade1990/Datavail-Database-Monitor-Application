
package com.datavail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
/**
 * @File_Desc:AgentUpdater Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :AgentApplication
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 * 
 */
@SpringBootApplication
@Profile({ "DeltaAgentUpdater", "Default" })
public class AgentUpdaterApplication {

	protected static Logger updaterlogger = LogManager.getLogger(AgentUpdaterApplication.class);

	public static void main(String[] args) throws Exception {
		try {
			SpringApplication.run(AgentUpdaterApplication.class, args);
		} catch (Exception ex)

		{
			updaterlogger.info("Updater Exception occurs " + ex.toString());
		}
		Properties properties = getConfigurationProperty();
		String sleepinterval = properties.getProperty("sleepinterval");
		String serverurl = properties.getProperty("serverurl");
		String localconfigfile = properties.getProperty("localconfigfile");
		String remoteconfigfile = properties.getProperty("remoteconfigfile");
		String pluginfilepath = properties.getProperty("pluginfilepath");
		String linuxpassword = properties.getProperty("linuxpassword");
		String serverID = serverId();
		String springbootConfigPath = properties.getProperty("springbootconfigpath");

		int sleepInterval = Integer.parseInt(sleepinterval);
		String fullClientURL = serverurl + "server/config/" + serverID;

		String tempPath = "temp/";

		String cmd1 = "service deltaagent status |grep 'Not running'";
		String Status = RunLinuxGrepCommand(cmd1);

		// System.out.println("StatusStatus AGENTUPDATER1" + Status);
		Status = Status.trim();
		if (!Status.contains("Error:") && !Status.equals("")) {
			// System.out.println("StatusStatus AGENTUPDATER2" + Status);
			startAgentService(linuxpassword);
		}
		// System.out.println("StatusStatus AGENTUPDATER3" + Status);

		while (true) {

			// --ondemand Config Check
			String ondemandClientURL = serverurl + "server/OnDemandConfig/" + serverID;
			File newondemandConfigFile = new File("DeltaOndemandMetric.xml");
			String newondemand = downloadNewPluginConfiguration(ondemandClientURL, newondemandConfigFile,
					"OnDemand Config");
			if (!newondemand.isEmpty()) {
				boolean success = stopAgentService(linuxpassword);
				if (success) {
					try {

						String ondemandURL = serverurl + "Server/PostOnDemandMetricStatus/";

						Client client = Client.create();

						WebResource webResource = client.resource(ondemandURL);
						try {
							ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
									.type("application/json").post(ClientResponse.class, "Posted");

							// logger.info("LinuxRam POST response: " +
							// response);
							updaterlogger.info("OnDemand metric status 'Y' to 'N' posted sucessfully:" + response);

							FileUtils.forceDelete(newondemandConfigFile);
						} catch (Exception ex1) {

						}

					} catch (Exception ex) {
						updaterlogger.info("Error while posting onDemand metric status.");
					}
				}
				startAgentService(linuxpassword);
			}
			//

			File newConfigurationFile = new File(remoteconfigfile);
			File oldConfigurationFile = new File(localconfigfile);
			try {
				downloadNewPluginConfiguration(fullClientURL, newConfigurationFile, "Config");
				if (!FileUtils.contentEquals(newConfigurationFile, oldConfigurationFile)) {
					HashMap<String, HashMap<String, String>> oldConfigData = getAgentConfigFileData(
							oldConfigurationFile);
					HashMap<String, HashMap<String, String>> newConfigData = getAgentConfigFileData(
							newConfigurationFile);

					for (HashMap.Entry<String, HashMap<String, String>> entry : newConfigData.entrySet()) {
						String newPluginVersion = entry.getValue().get("AdapterVersion");
						String newPluginAdapterAssembly = entry.getValue().get("AdapterAssembly");
						HashMap<String, String> oldPluginMap = oldConfigData.get(entry.getKey());
						String oldPluginVersion = "";
						String oldPluginAdapterAssembly = "";
						if (oldPluginMap != null) {
							oldPluginVersion = oldPluginMap.get("AdapterVersion");
							oldPluginAdapterAssembly = oldPluginMap.get("AdapterAssembly");
						}

						if (newPluginAdapterAssembly == oldPluginAdapterAssembly
								&& !newPluginVersion.equals(oldPluginVersion)) {
							// String newPluginAdapterAssembly =
							// entry.getValue().get("AdapterAssembly");
							String downloadURL = serverurl + "Assembly/" + newPluginAdapterAssembly + "/"
									+ newPluginVersion + ".jar";

							String filePath = tempPath + newPluginAdapterAssembly + ".jar";
							downloadUpdatedPlugin(downloadURL, filePath);
						} else if (oldPluginAdapterAssembly == "") {
							String downloadURL = serverurl + "Assembly/" + newPluginAdapterAssembly + "/"
									+ newPluginVersion + ".jar";

							String filePath = tempPath + newPluginAdapterAssembly + ".jar";
							downloadUpdatedPlugin(downloadURL, filePath);
						}

					}
					boolean success = stopAgentService(linuxpassword);
					if (success) {

						createNewSpringbootConfig(newConfigData, springbootConfigPath);

						copyAndDeleteFiles(pluginfilepath, tempPath, newConfigurationFile, oldConfigurationFile);

						RunLinuxCommand("chmod 777 -R lib/");
						startAgentService(linuxpassword);

					} else {
						updaterlogger.info("ERROR STOPING AGENT. Will not copy new files");
						System.out.println("ERROR STOPING AGENT. Will not copy new files");
					}
					updaterlogger.info("Success running AgentUpdater");
					System.out.println("Success running AgentUpdater");
				} else {
					updaterlogger.info("No need to update Agent. Everything is up-to-date");
					System.out.println("No need to update Agent. Everything is up-to-date");
				}
			} catch (Exception ex) {

			}

			int timeinterval = 1000 * 60;
			Thread.sleep(timeinterval * sleepInterval);
		}

	}

	public static String RunLinuxCommand(String cmd) throws IOException {
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

	public static String serverId() throws ClassNotFoundException, SQLException, IOException {
		String ServerId = "";

		ServerId = RunLinuxCommand("cat /sys/class/dmi/id/product_uuid");

		if (ServerId == "") {
			ServerId = RunLinuxCommand("hostid");
		} else {
			ServerId = ServerId.replaceAll("UUID:", "");
		}

		return ServerId;

	}

	public static HashMap<String, HashMap<String, String>> getAgentConfigFileData(File fXmlFile)
			throws SAXException, IOException, ParserConfigurationException {
		HashMap<String, HashMap<String, String>> outterMap = new HashMap<String, HashMap<String, String>>();

		Element eElement = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("MetricInstance");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			HashMap<String, String> innerMap = new HashMap<String, String>();

			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				eElement = (Element) nNode;
				innerMap.put("AdapterAssembly", eElement.getAttribute("AdapterAssembly"));
				innerMap.put("AdapterClass", eElement.getAttribute("AdapterClass"));
				innerMap.put("AdapterVersion", eElement.getAttribute("AdapterVersion"));
				innerMap.put("ScheduleInterval", eElement.getAttribute("ScheduleInterval"));
				innerMap.put("ScheduleType", eElement.getAttribute("ScheduleType"));
				innerMap.put("Data", eElement.getAttribute("Data"));
				innerMap.put("Id", eElement.getAttribute("Id"));
				outterMap.put(eElement.getAttribute("Id"), innerMap);
			}
		}
		return outterMap;

	}

	public static Properties getConfigurationProperty() throws InvalidPropertiesFormatException, IOException {
		File file = new File("config.xml");
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.loadFromXML(fileInput);
		fileInput.close();
		return properties;
	}

	public static void downloadUpdatedPlugin(String urlLink, String filePath) throws IOException {
		updaterlogger.info("Downloading updated JAR file: " + urlLink);
		System.out.println("Downloading updated JAR file: " + urlLink);
		URL link = new URL(urlLink);
		try {
			String response = IOUtils.toString(link.openStream());
			JSONObject jsonObj = new JSONObject(response);
			String pluginData = jsonObj.getString("contents");
			byte[] decoded = Base64.getDecoder().decode(pluginData);
			FileUtils.writeByteArrayToFile(new File(filePath), decoded);
		} catch (Exception ex) {
		}

	}

	public static String downloadNewPluginConfiguration(String clienturl, File newConfigurationFile, String filename)
			throws IOException {
		String pluginData = "";
		try {
			URL url = new URL(clienturl);

			String response = IOUtils.toString(url.openStream());
			JSONObject jsonObj = new JSONObject(response);

			try {
				pluginData = jsonObj.getString("configuration");
			}

			catch (Exception ex) {
				pluginData = jsonObj.getString("Configuration");
			}
			// System.out.println("pluginData: " + pluginData);
			if (!pluginData.isEmpty()) {
				FileUtils.writeStringToFile(newConfigurationFile, pluginData);
				updaterlogger.info("Downloading new " + filename + " file: " + clienturl);
				System.out.println("Downloading new " + filename + " file: " + clienturl);

			}
		} catch (Exception ex1) {
		}

		return pluginData;

	}

	public static void copyAndDeleteFiles(String destFilePath, String srcFilePath, File newConfigurationFile,
			File oldConfigurationFile) throws IOException {
		FileUtils.copyFile(newConfigurationFile, oldConfigurationFile);
		FileUtils.forceDelete(newConfigurationFile);

		File destFile = new File(destFilePath);
		File srcFile = new File(srcFilePath);
		FileUtils.copyDirectory(srcFile, destFile);
		FileUtils.forceDelete(srcFile);
	}

	public static void createNewSpringbootConfig(HashMap<String, HashMap<String, String>> newConfigData,
			String springbootConfigPath) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("beans");
		doc.appendChild(rootElement);
		rootElement.setAttribute("xmlns", "http://www.springframework.org/schema/beans");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xsi:schemaLocation",
				"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd");
		int intid = 0;
		for (HashMap.Entry<String, HashMap<String, String>> entry : newConfigData.entrySet()) {

			String newScheduleInterval = entry.getValue().get("ScheduleInterval");
			String newScheduleType = entry.getValue().get("ScheduleType");
			String newAdapterAssembly = entry.getValue().get("AdapterAssembly");
			String newData = entry.getValue().get("Data");
			String newMetricInstanceId = entry.getValue().get("Id");
			String newConnectionString = "";
			String newConnectionStrings = "";

			int startindex = 0;
			int endindex = 0;
			try {
				startindex = newData.lastIndexOf("ConnectionString");
				endindex = newData.lastIndexOf("InstanceName");
				String latest = newData.substring(startindex, endindex);
				latest.trim();
				newConnectionString = latest.replace("ConnectionString=", "");
				newConnectionStrings = newConnectionString.replaceAll("\"", "");
			} catch (Exception e) {
			}

			String newThresholdString = "";
			String newThresholdStrings = "";

			try {
				startindex = newData.lastIndexOf("Threshold");
				endindex = newData.lastIndexOf("Label");
				String latest = newData.substring(startindex, endindex);

				latest.trim();

				newThresholdString = latest.replace("Threshold=", "");
				newThresholdStrings = newThresholdString.replaceAll("\"", "");
				newThresholdStrings.trim();
			} catch (Exception e) {
			}

			intid = intid + 1;
			String cleanAdapterAssembly = newAdapterAssembly.substring(newAdapterAssembly.lastIndexOf(".") + 1);
			// bean elements
			Element bean = doc.createElement("bean");
			rootElement.appendChild(bean);
			bean.setAttribute("id", cleanAdapterAssembly + intid);
			String adapterClass = cleanAdapterAssembly.toLowerCase().substring(0, cleanAdapterAssembly.length()) + "."
					+ cleanAdapterAssembly;
			bean.setAttribute("class", adapterClass);
			// property elements
			Element nameProperty = doc.createElement("property");
			bean.appendChild(nameProperty);
			nameProperty.setAttribute("name", "name");
			nameProperty.setAttribute("value", cleanAdapterAssembly);

			Element intervalProperty = doc.createElement("property");
			bean.appendChild(intervalProperty);
			intervalProperty.setAttribute("name", "sleepInterval");
			intervalProperty.setAttribute("value", newScheduleInterval);

			Element scheduleTypeProperty = doc.createElement("property");
			bean.appendChild(scheduleTypeProperty);
			scheduleTypeProperty.setAttribute("name", "scheduleType");
			scheduleTypeProperty.setAttribute("value", newScheduleType);

			Element ConnectionStringProperty = doc.createElement("property");
			bean.appendChild(ConnectionStringProperty);
			ConnectionStringProperty.setAttribute("name", "ConnectionString");
			ConnectionStringProperty.setAttribute("value", newConnectionStrings);

			Element threshold = doc.createElement("property");
			bean.appendChild(threshold);
			threshold.setAttribute("name", "thershold");
			threshold.setAttribute("value", newThresholdStrings);

			Element metricInstanceId = doc.createElement("property");
			bean.appendChild(metricInstanceId);
			metricInstanceId.setAttribute("name", "metricInstanceId");
			metricInstanceId.setAttribute("value", newMetricInstanceId);
		}

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(springbootConfigPath));

		transformer.transform(source, result);

		System.out.println("New springboot file created!");
	}

	public static boolean startAgentService(String linuxPassword) throws IOException, InterruptedException {
		String executeCmd = "echo '" + linuxPassword + "' | sudo -S service deltaagent start";
		Process runtimeProcess = Runtime.getRuntime().exec(new String[] { "bash", "-c", executeCmd });
		runtimeProcess.waitFor();

		if (runtimeProcess.exitValue() != 0) {
			updaterlogger.info("Error while executing start script");
			System.out.println("Error while executing start script");
			return false;
		} else {
			updaterlogger.info("Agent Started");
			System.out.println("Agent Started");
			return true;
		}

	}

	public static boolean stopAgentService(String linuxPassword) throws IOException, InterruptedException {
		String executeCmd = "echo '" + linuxPassword + "' | sudo -S service deltaagent stop";
		Process runtimeProcess = Runtime.getRuntime().exec(new String[] { "bash", "-c", executeCmd });
		runtimeProcess.waitFor();

		if (runtimeProcess.exitValue() != 0) {
			updaterlogger.info("Error while executing stop script");
			System.out.println("Error while executing stop script");
			return false;
		} else {
			updaterlogger.info("Agent Stopped");
			System.out.println("Agent Stopped");
			return true;
		}

	}

	public static String RunLinuxGrepCommand(String command) {
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
			// e.printStackTrace(pw);
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

}
