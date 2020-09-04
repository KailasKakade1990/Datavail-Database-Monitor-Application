package checkin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.datavail.plugins.BasePlugin;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import connection.MysqlConnection;
import pluginmessages.CommonPluginMessages;
/**
 * @File_Desc:Checkin Plugin  Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :AgentApplication
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class CheckIn extends BasePlugin {
	private String name;
	private int sleepInterval;

	private String data = "";
	private String checkInMessage = "";
	private String ConnectionString;
	private int scheduleType;
	private String thershold;
	private String metricInstanceId;

	@Override
	public void run() {
		try {
			CommonPluginMessages CPM = new CommonPluginMessages();

			String serverID = CPM.serverId();
			MysqlConnection mysqlconn = new MysqlConnection();
			WebResource webResource1 = mysqlconn.client1();
			while (true) {
				updateData();
				String postData = getPostData();

				logger.info("CheckIn DATA sent: " + postData);

				try {
					ClientResponse response = webResource1.path("/{id}").queryParam("id", serverID)
							.type("application/json").post(ClientResponse.class, postData);
					logger.info("CheckIn POST response: " + response);
				} catch (Exception ex) {
					logger.error("Checkin POST ERROR:"+ ex.toString());

				}

				int timeinterval = CPM.buildSchedule(scheduleType, sleepInterval);

				if (timeinterval != 0) {
					Thread.sleep(timeinterval);
				}

			}
		} catch (ClassNotFoundException |

				SQLException e1) {
			logger.error("CheckIn threw error, full stack trace follows:" + e1);
			// e1.printStackTrace();
		} catch (IOException e2) {
			logger.error("CheckIn threw error, full stack trace follows:" + e2);
			// e2.printStackTrace();
		} catch (InterruptedException e3) {
			logger.error("CheckIn threw error, full stack trace follows:" + e3);
			// e3.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (Exception ex) {
			logger.error("CheckIn Threw ERROR:" + ex.toString());

		}
	}

	public void updateData() throws ClassNotFoundException, SQLException {

		String adapterclass = "CheckIn";
		CommonPluginMessages commonmessages = new CommonPluginMessages();
		checkInMessage = commonmessages.checkinmessage(adapterclass);

		String hostname = "";
		String s = "";
		try {

			Process p = Runtime.getRuntime().exec("hostname");

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			while ((s = stdInput.readLine()) != null) {

				hostname = s;
			}

			while ((s = stdError.readLine()) != null) {
				hostname = null;

			}

		} catch (IOException e) {
			// e.printStackTrace();
		}

		data = hostname;
	}

	public String getPostData() throws FileNotFoundException, IOException, XmlPullParserException {

		CommonPluginMessages CPM = new CommonPluginMessages();

		String Hostname = "\"Hostname\":\"" + data + "\"" + ",";
		String agentVersion = "\"AgentVersion\":\"" + CPM.getVersionNumber() + "\"" + ",";

		InetAddress addr = InetAddress.getLocalHost();

		String IpAddress = "\"IpAddress\":\"" + addr.getHostAddress() + "\"" + ",";

		String TenantId = "\"TenantId\":\"1a19a18a-846c-49da-93c1-8948afdc0151\"" + ",";
		java.util.Date date = new java.util.Date();
		String Timestamp = "\"Timestamp\":\"" + "\\" + "/" + "Date" + "(" + new Timestamp(date.getTime()) + ")" + "\\"
				+ "/" + "\"" + "," + "\"Id\":null" + "," + "\"PopReceipt\":0" + "," + "\"DequeueCount\":0";

		String postData1 = "{" + Hostname + " " + IpAddress + " " + TenantId + " " + agentVersion + " " + Timestamp
				+ "}";
		return postData1;
	}

	public String getAgentLogMessage() {

		java.util.Date date = new java.util.Date();
		Timestamp checkindate = new Timestamp(date.getTime());

		String AgentLog_Message = "INFO" + " " + checkindate + " " + "[10]:" + " " + "Check-In PlugIn Executed." + " "
				+ checkInMessage + "\n\n";

		return AgentLog_Message;

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