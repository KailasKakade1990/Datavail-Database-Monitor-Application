package linuxoraclehealthcheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

import pluginmessages.CommonPluginMessages;
/**
 * @PluginDesc:Oracle Health Check On Linux
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :Linux <> Plugin
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 */
public class LinuxCpuPlugin extends BasePlugin {
	private String name;
	private int sleepInterval;
	private int scheduleType;
	private String ConnectionString;
	private String resultStatus = "";
	private String thershold;
	
public int excuteLevel;
	
	public LinuxCpuPlugin()
	{
		excuteLevel=0;
	}
	@Override
	public void run() {

		// System.out.println("Inside linux CPU Plugin");

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

		// CPU(s):
		String cmd = "lscpu | grep ^CPU'(s)':";
		String postData = "";
		String cpus = cpm.RunLinuxGrepCommand(cmd);

		String _cpus = "";
		if (!cpus.contains("Error:")) {

			cpus = cpus.replace("CPU(s):", "");

			cpus = cpus.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpus.split(" ");

			_cpus = strArr[0];
		}

		// online CPU
		cmd = "lscpu | grep On-line";

		String onlinecpu = cpm.RunLinuxGrepCommand(cmd);

		String _onlinecpuval = "";
		if (!onlinecpu.contains("Error:")) {

			onlinecpu = onlinecpu.replace("On-line CPU(s) list:", "");

			onlinecpu = onlinecpu.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = onlinecpu.split(" ");

			_onlinecpuval = strArr[0];
		}
		// CPU thread
		cmd = "lscpu | grep Thread";
		String threadspercore = cpm.RunLinuxGrepCommand(cmd);

		String _threadspercore = "";
		if (!threadspercore.contains("Error:")) {

			threadspercore = threadspercore.replace("Thread(s) per core:", "");

			threadspercore = threadspercore.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = threadspercore.split(" ");

			_threadspercore = strArr[0];
		}
		// cpu cores
		cmd = "lscpu | grep Core";
		String cpucores = cpm.RunLinuxGrepCommand(cmd);

		String _cpucores = "";
		if (!cpucores.contains("Error:")) {

			cpucores = cpucores.replace("Core(s) per socket:", "");

			cpucores = cpucores.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpucores.split(" ");

			_cpucores = strArr[0];
		}

		// cpu Sockets
		cmd = " lscpu | grep Socket";
		String cpusockets = cpm.RunLinuxGrepCommand(cmd);

		String _cpusockets = "";
		if (!cpusockets.contains("Error:")) {

			cpusockets = cpusockets.replace("Socket(s):", "");

			cpusockets = cpusockets.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpusockets.split(" ");

			_cpusockets = strArr[0];
		}

		// CPU NUMA nodes
		cmd = "  lscpu | grep node'(s)'";
		String numnodes = cpm.RunLinuxGrepCommand(cmd);

		String _numnodes = "";
		if (!numnodes.contains("Error:")) {

			numnodes = numnodes.replace("NUMA node(s):", "");

			numnodes = numnodes.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = numnodes.split(" ");

			_numnodes = strArr[0];
		}

		// CPU family
		cmd = "lscpu | grep family";
		String cpufamily = cpm.RunLinuxGrepCommand(cmd);

		String _cpufamily = "";
		if (!cpufamily.contains("Error:")) {

			cpufamily = cpufamily.replace("CPU family:", "");

			cpufamily = cpufamily.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpufamily.split(" ");

			_cpufamily = strArr[0];
		}

		// CPU Model:
		cmd = "lscpu | grep Model:";
		String cpumodel = cpm.RunLinuxGrepCommand(cmd);

		String _cpumodel = "";
		if (!cpumodel.contains("Error:")) {

			cpumodel = cpumodel.replace("Model:", "");

			cpumodel = cpumodel.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpumodel.split(" ");

			_cpumodel = strArr[0];
		}

		// CPU Model name:
		cmd = "lscpu | grep name:";
		String cpumodelname = cpm.RunLinuxGrepCommand(cmd);

		String _cpumodelname = "";
		if (!cpumodelname.contains("Error:")) {

			cpumodelname = cpumodelname.replace("Model name:", "");

			cpumodelname = cpumodelname.trim().replaceAll("\\s{2,}", " ");
			// String[] strArr = cpumodelname.split(" ");

			_cpumodelname = cpumodelname;
		}

		// CPU Stepping:
		cmd = "lscpu | grep Stepping:";
		String cpustepping = cpm.RunLinuxGrepCommand(cmd);

		String _cpustepping = "";
		if (!cpustepping.contains("Error:")) {

			cpustepping = cpustepping.replace("Stepping:", "");

			cpustepping = cpustepping.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpustepping.split(" ");

			_cpustepping = strArr[0];
		}

		// CPU MHz::
		cmd = "lscpu | grep MHz:";
		String cpumhz = cpm.RunLinuxGrepCommand(cmd);

		String _cpumhz = "";
		if (!cpumhz.contains("Error:")) {

			cpumhz = cpumhz.replace("CPU MHz:", "");

			cpumhz = cpumhz.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpumhz.split(" ");

			_cpumhz = strArr[0];
		}

		// CPU BogoMIPS:
		cmd = "lscpu | grep BogoMIPS:";
		String cpubogomips = cpm.RunLinuxGrepCommand(cmd);

		String _cpubogomips = "";
		if (!cpubogomips.contains("Error:")) {

			cpubogomips = cpubogomips.replace("BogoMIPS:", "");

			cpubogomips = cpubogomips.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpubogomips.split(" ");

			_cpubogomips = strArr[0];
		}

		// Virtualization type
		cmd = "lscpu | grep Virtualization";
		String cpuvirtualization = cpm.RunLinuxGrepCommand(cmd);

		String _cpuvirtualization = "";
		if (!cpuvirtualization.contains("Error:")) {

			cpuvirtualization = cpuvirtualization.replace("Virtualization type:", "");

			cpuvirtualization = cpuvirtualization.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpuvirtualization.split(" ");

			_cpuvirtualization = strArr[0];
		}

		// L1d cache:
		cmd = "lscpu | grep L1d";
		String cpuL1d = cpm.RunLinuxGrepCommand(cmd);

		String _cpuL1d = "";
		if (!cpuL1d.contains("Error:")) {

			cpuL1d = cpuL1d.replace("L1d cache:", "");

			cpuL1d = cpuL1d.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpuL1d.split(" ");

			_cpuL1d = strArr[0];
		}

		// L1i cache:
		cmd = "lscpu | grep L1i";
		String cpuLid = cpm.RunLinuxGrepCommand(cmd);

		String _cpuLid = "";
		if (!cpuLid.contains("Error:")) {

			cpuLid = cpuLid.replace("L1i cache:", "");

			cpuLid = cpuLid.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpuLid.split(" ");

			_cpuLid = strArr[0];
		}

		// L2 cache:
		cmd = "lscpu | grep L2";
		String cpul2cache = cpm.RunLinuxGrepCommand(cmd);

		String _cpul2cache = "";
		if (!cpul2cache.contains("Error:")) {

			cpul2cache = cpul2cache.replace("L2 cache:", "");

			cpul2cache = cpul2cache.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpul2cache.split(" ");

			_cpul2cache = strArr[0];
		}

		// L3 cache:
		cmd = "lscpu | grep L3";
		String cpul3cache = cpm.RunLinuxGrepCommand(cmd);

		String _cpul3cache = "";
		if (!cpul3cache.contains("Error:")) {

			cpul3cache = cpul3cache.replace("L3 cache:", "");

			cpul3cache = cpul3cache.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpul3cache.split(" ");

			_cpul3cache = strArr[0];
		}
		// NUMA node0 CPU(s):

		cmd = "lscpu | grep node0";
		String cpunode0 = cpm.RunLinuxGrepCommand(cmd);

		String _cpunode0 = "";
		if (!cpunode0.contains("Error:")) {

			cpunode0 = cpunode0.replace("NUMA node0 CPU(s):", "");

			cpunode0 = cpunode0.trim().replaceAll("\\s{2,}", " ");
			String[] strArr = cpunode0.split(" ");

			_cpunode0 = strArr[0];
		}

		// if (onlinecpu.contains("Error:")) {
		// to do check agent error status
		/*
		 * String errorPostData = cpm.buildErrorString("LinuxRamPlugin",
		 * "PostData", memory, ServerId, str_metricInstanceId, formattedTime,
		 * str_product, IpAddress);
		 * 
		 * if (!errorPostData.isEmpty()) { logger.info("AgentError DATA sent: "
		 * + errorPostData);
		 * 
		 * String serverID = cpm.serverId();
		 * 
		 * MysqlConnection oracleconn = new MysqlConnection();
		 * 
		 * WebResource webResource = oracleconn.client();
		 * 
		 * ClientResponse response = webResource.path("/{id}").queryParam("id",
		 * serverID).type("application/json") .post(ClientResponse.class,
		 * errorPostData); logger.info("AgentError POST response: " + response);
		 * }
		 */

		// } else {

		postData = "\\u003cMonitor ";
		postData = postData + " DBInstanceID=\\\"" + "" + "\\\"";
		postData = postData + " DatabaseID=\\\"" + "" + "\\\"";
		postData = postData + " AgentVersion=\\\"" + "6.0.0.0" + "\\\"";
		postData = postData + " DateTime=\\\"" + formattedTime + "\\\"";
		postData = postData + " MonitorInstanceID=\\\"" + str_metricInstanceId + "\\\"";
		postData = postData + " MonitorName=\\\"" + "LinuxCpuHealthCheckPlugin" + "\\\"";
		postData = postData + " TimeZone=\\\"" + "India Standard Time" + "\\\"";
		postData = postData + " Culture=\\\"" + "en-IN" + "\\\"";

		postData = postData + "\\u003e";

		postData = postData + "\\u003cProperties\\u003e";

		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "1" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "On Line CPU(s) List" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "1" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _onlinecpuval + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "2" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "Thread(s) per core" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "2" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _threadspercore + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// CPU(s):

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "3" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "CPU(s):" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "3" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpus + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// Socket(s):

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "4" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "Socket(s):" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "4" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpusockets + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// CPU family:
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "5" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "CPU family:" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "5" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpufamily + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// CPU Model
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "6" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "Model:" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "6" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpumodel + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// CPU Model name
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "7" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "Model name:" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "7" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpumodelname + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// CPU Stepping:
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "8" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "CPU Stepping:" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "8" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpustepping + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// CPU MHz:
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "9" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "CPU MHz:" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "9" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpumhz + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// CPU BogoMIPS:
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "10" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "CPU BogoMIPS:" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "10" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpubogomips + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// Virtualization type:
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "11" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "Virtualization type:" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "11" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpuvirtualization + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// L1d cache:
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "12" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "L1d cache:" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "12" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpuL1d + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// L1i cache:
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "13" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "L1i cache:" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "13" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpuLid + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// L2 cache:
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "14" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "L2 cache:" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "14" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpul2cache + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// L3 cache:
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "15" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "L3 cache:" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "15" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpul3cache + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// NUMA node0 CPU(s):
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "16" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "NUMA node0 CPU(s):" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "16" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpunode0 + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// NUMA node(s):
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "17" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "NUMA node(s):" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "17" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _numnodes + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		// CPU(s) cores:
		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "18" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Key" + "\\\"";
		postData = postData + " Value=\\\"" + "Core(s) per socket:" + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";

		postData = postData + "\\u003cProperties\\u003e";
		postData = postData + "\\u003cProperty";
		postData = postData + " Type=\\\"" + "Text" + "\\\"";
		postData = postData + " Row=\\\"" + "18" + "\\\"";
		postData = postData + " Key=\\\"" + "Detail Value" + "\\\"";
		postData = postData + " Value=\\\"" + _cpucores + "\\\"";
		postData = postData + " /\\u003e";
		postData = postData + "\\u003c/Properties\\u003e";
		postData = postData + "\\u003c/Monitor";
		postData = postData + "\\u003e";

		// }

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
