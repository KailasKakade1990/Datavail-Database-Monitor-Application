package linorafilesystemsizestatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

import connection.MysqlConnection;
import connection.OracleConnection;
import pluginmessages.CommonPluginMessages;
/**
 * @File_Desc:Oracle File Size Plugin  Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :LinOraFileSystemSizeStatus
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class LinOraFileSystemSizeStatus extends BasePlugin {
	private String name;
	private int sleepInterval;

	private String ConnectionString;
	private String result = "";
	private String resultCode = "";
	private int scheduleType;
	private String resultMessage = "";
	private String thershold;
	String postData = "";
	String serverID = "";
	String status = "";

	@Override
	public void run() {
		try {

			CommonPluginMessages CPM = new CommonPluginMessages();

			serverID = CPM.serverId();
			OracleConnection oracleconn = new OracleConnection();

			WebResource webResource = oracleconn.client();
			while (true) {
				updateData();
				if (!postData.isEmpty()) {

					postData = getPostData();
					logger.info("LinOraFileSystemSizeStatus DATA sent: " + postData);

					try {
						ClientResponse response = webResource.path("/{id}").queryParam("id", serverID)
								.type("application/json").post(ClientResponse.class, postData);
						logger.info("LinOraFileSystemSizeStatus POST response: " + response);
					}

					catch (Exception ex) {

						logger.error(" LinOraFileSystemSizeStatus POST ERROR:"+ex.toString());
					}
				}

				int timeinterval = CPM.buildSchedule(scheduleType, sleepInterval);

				if (timeinterval != 0) {
					Thread.sleep(timeinterval);
				}
			}
		} catch (ClassNotFoundException | SQLException e1) {
			logger.error("LinOraFileSystemSizeStatus threw error, full stack trace follows:"+ e1);
			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("LinOraFileSystemSizeStatus threw error, full stack trace follows:"+ e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("LinOraFileSystemSizeStatus threw error, full stack trace follows:"+ e3);
			// e3.printStackTrace();

		} catch (XPathExpressionException e) {

			// e.printStackTrace();
		} catch (ParserConfigurationException e) {

			// e.printStackTrace();
		} catch (SAXException e) {

			// e.printStackTrace();
		} catch (Exception ex) {
			logger.error("LinOraFileSystemSizeStatus Threw ERROR:"+ ex.toString());

		}

	}

	public void updateData() throws ClassNotFoundException, SQLException, InvalidPropertiesFormatException, IOException,
			XPathExpressionException, ParserConfigurationException, SAXException {

		ResultSet rs = null;
		try {
			OracleConnection oracleconn = new OracleConnection();

			Connection Con1 = oracleconn.beanConnection(ConnectionString);
			Statement st = (Statement) Con1.createStatement();

			if (Con1 != null) {

				StringBuilder query = new StringBuilder();
				query.append(" select * from ( ");
				query.append(" SELECT inn1.tablespace_name tbs_name ");
				query.append(" ,ROUND(((inn1.allocated-NVL(inn2.free,0))/inn1.total)*100) USED_PERCENT");
				query.append(" FROM ");
				query.append(
						"( select tablespace_name,SUM(bytes)/1024/1024 allocated,SUM(DECODE(autoextensible,'YES',maxbytes,bytes))/1024/1024 total");
				query.append(" FROM dba_data_files GROUP BY tablespace_name) inn1, ");
				query.append(" (select tablespace_name,NVL(SUM(bytes)/1024/1024,0) free");
				query.append(" FROM dba_free_space GROUP BY tablespace_name) inn2 ");
				query.append(" WHERE inn1.tablespace_name= inn2.tablespace_name");
				query.append(" order by 2 desc ) where USED_PERCENT > " + thershold);

				String ag = query.toString();

				rs = (ResultSet) st.executeQuery(ag);
				result = "";
				while (rs.next()) {

					String longProcessThreshold = thershold;
					String tableSpaceName = rs.getString("tbs_name");
					String usedPercent = rs.getString("USED_PERCENT");

					resultCode = "0";
					resultMessage = "'" + tableSpaceName + "' File system size is over threshold : " + usedPercent;

					result = result + "  \\u003cFileSystemSizeStatusResult resultCode=\\\"" + resultCode
							+ "\\\" resultMessage=\\\"" + resultMessage + "\\\" ";
					result = result + " longProcessThreshold=\\\"" + longProcessThreshold + "\\\"";
					result = result + "/\\u003e\\r\\n";

				}

			}

			rs.close();
			Con1.close();
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			// e.printStackTrace(pw);
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

	public String getPostData() throws XPathExpressionException, InvalidPropertiesFormatException, IOException,
			ParserConfigurationException, SAXException, ClassNotFoundException, SQLException {

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

		String xPathString = "/AgentConfiguration/MetricInstance[@AdapterClass='LinuxOracleFileSystemSizeStatusPlugin']";

		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression xPathExpression = xPath.compile(xPathString);
		Node nNode = (Node) xPathExpression.evaluate(doc, XPathConstants.NODE);

		Element eElement = (Element) nNode;
		String str_metricInstanceId = eElement.getAttribute("Id");
		String str_label = eElement.getAttribute("Label");

		String str_product = eElement.getAttribute("product");
		String newData = eElement.getAttribute("Data");

		String newInstanceName = "";
		String newInstanceNames = "";
		String str_instanceName = "";
		int startindex = 0;
		int endindex = 0;

		startindex = newData.lastIndexOf("InstanceName");
		endindex = newData.lastIndexOf("InstanceId");
		String latest = newData.substring(startindex, endindex);
		latest.trim();
		newInstanceName = latest.replace("InstanceName=", "");
		newInstanceNames = newInstanceName.replaceAll("\"", "");
		newInstanceNames.trim();
		str_instanceName = newInstanceNames;
		CommonPluginMessages cm = new CommonPluginMessages();
		String hostname = cm.RunLinuxCommand("hostname");

		InetAddress addr = InetAddress.getLocalHost();
		String IpAddress = addr.getHostAddress();

		System.currentTimeMillis();
		OracleConnection orcconn = new OracleConnection();
		String product = orcconn.runOracleQuery(ConnectionString,
				"select banner from v$version where banner like 'Oracle%'");
		String pd = "";
		if (!product.contains("Error:") && product != "") {
			pd = product;
		}

		String str_Result = orcconn.runOracleQuery(ConnectionString,
				"select banner from v$version where banner like 'PL%'");
		String str_Version = "";
		if (!str_Result.contains("Error:") && str_Result != "") {
			str_Version = str_Result;
		}

		String product_level = orcconn.runOracleQuery(ConnectionString,
				"select banner from v$version where banner like 'CORE%'");
		String pl = "";
		if (!product_level.contains("Error:") && product_level != "") {
			pl = product_level;
		}

		String product_edition = orcconn.runOracleQuery(ConnectionString,
				"select banner from v$version where banner like 'TNS%'");
		String pe = "";

		if (!product_edition.contains("Error:") && product_edition != "") {
			pe = product_level;
		}

		if (status.contains("Error:")) {
			// to do check agent error status
			String errorPostData = cm.buildErrorString("LinOraFileSystemSizeStatus", "PostData", status, serverID,
					str_metricInstanceId, formattedTime, str_product, IpAddress);

			if (!errorPostData.isEmpty()) {
				logger.info("AgentError DATA sent: " + errorPostData);

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

		}

		else {

			postData = "{" + "\"" + "Data" + "\"" + ":" + "\"" + "\\" + "u003c" + "FileSystemSizeStatusPluginOutput ";
			postData = postData + " timestamp=\\\"" + formattedTime + "\\\"";
			postData = postData + " product=\\\"" + pd + "\\\"";
			postData = postData + " productVersion=\\\"" + str_Version + "\\\"";
			postData = postData + " productLevel=\\\"" + pl + "\\\"";
			postData = postData + " productEdition=\\\"" + pe + "\\\"";
			postData = postData + " metricInstanceId=\\\"" + str_metricInstanceId + "\\\"";
			postData = postData + " label=\\\"" + str_label + "\\\"";
			postData = postData + " instanceName=\\\"" + str_instanceName + "\\\"";
			postData = postData + "\\u003e\\r\\n";
			postData = postData + result;
			postData = postData + "\\u003c/FileSystemSizeStatusPluginOutput\\u003e\",";
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

}
