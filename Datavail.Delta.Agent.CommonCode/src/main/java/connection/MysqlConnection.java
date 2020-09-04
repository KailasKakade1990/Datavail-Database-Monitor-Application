package connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
/**
 * @File_Desc:Common Code Application
 * @OS :Linux Red Hat 4.8.5-16 & Linux Ubuntu (16.04)
 * @FileName :AgentApplication
 * @author : Kailas Kakade
 * @version : 1.0
 * @since :September-2017-2018
 * @email: kailas.kakade@datavail.com
 * @last_Modified:
 */
public class MysqlConnection {

	String linux_host_name = "", linuxmysql_user_name = "", linuxmysql_pass_word = "";
	String linux_host_name1 = "", linuxmysql_user_name1 = "", linuxmysql_pass_word1 = "";
	String linux_hostname = "", linux_username = "", linux_password = "", linux_command = "";
	String ndbcluster_hostname = "", ndbcluster_username = "", ndbcluster_password = "", ndbcluster_command = "";
	String client_url = "", client_url1 = "";

	public Connection startconnection() throws ClassNotFoundException, SQLException {
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			linux_host_name = properties.getProperty("linuxmysqlhost");
			linuxmysql_user_name = properties.getProperty("linuxmysqluser");
			linuxmysql_pass_word = properties.getProperty("linuxmysqlpass");

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		Class.forName("com.mysql.jdbc.Driver");
		Connection con = (Connection) DriverManager.getConnection(linux_host_name, linuxmysql_user_name,
				linuxmysql_pass_word);
		return con;
	}

	// Bean Configuration this code is about the connection
	// configuration of MySQL. Old code was reading connection through
	// config.xml file.
	/**
	 * @author Kailas.Kakade
	 *
	 */
	public Connection beanConnection(String hostDetails) throws ClassNotFoundException, SQLException {
		try {

			// System.out.println("Connection String values
			// ============================" + hostDetails);
			hostDetails.trim();
			String[] strArr = hostDetails.split(";");
			Map<String, String> map = new HashMap<String, String>();
			for (String s1 : strArr) {
				String[] str = s1.split("=");
				map.put(str[0], str[1]);
			}

			// System.out.println("Connection String values" + details);

			System.out.println("Map Connection values" + map);

			linux_host_name = map.get("linuxmysqlhost");
			linuxmysql_user_name = map.get("linuxmysqluser");
			linuxmysql_pass_word = map.get("linuxmysqlpass");

		} catch (Exception e) {
			// e.printStackTrace();
		}

		Class.forName("com.mysql.jdbc.Driver");
		Connection con = (Connection) DriverManager.getConnection(linux_host_name, linuxmysql_user_name,
				linuxmysql_pass_word);
		return con;
	}

	public Connection startconnection1() throws ClassNotFoundException, SQLException {
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			linux_host_name1 = properties.getProperty("linuxhost1");
			linuxmysql_user_name = properties.getProperty("linuxmysqluser");
			linuxmysql_pass_word = properties.getProperty("linuxmysqlpass");

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		Class.forName("com.mysql.jdbc.Driver");
		Connection con1 = (Connection) DriverManager.getConnection(linux_host_name1, linuxmysql_user_name,
				linuxmysql_pass_word);
		return con1;
	}

	public Channel remoteconnection() throws JSchException, IOException {
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			linux_hostname = properties.getProperty("linuxhost");
			linux_username = properties.getProperty("linuxuser");
			linux_password = properties.getProperty("linuxpass");
			linux_command = properties.getProperty("command");
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		JSch jsch = new JSch();
		Session session = jsch.getSession(linux_username, linux_hostname, 22);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setPassword(linux_password);
		session.connect();
		Channel channel = (Channel) session.openChannel("exec");
		((ChannelExec) channel).setCommand(linux_command);
		((ChannelExec) channel).setInputStream(null);
		((ChannelExec) channel).setErrStream(System.err);
		return channel;

	}

	public Channel Ndbclusterremoteconnection() throws JSchException, IOException {
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			ndbcluster_hostname = properties.getProperty("ndbcluster_host");
			ndbcluster_username = properties.getProperty("ndbcluster_user");
			ndbcluster_password = properties.getProperty("ndbcluster_pass");
			ndbcluster_command = properties.getProperty("ndbcluster_command");

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		JSch jsch = new JSch();
		Session session = jsch.getSession(ndbcluster_username, ndbcluster_hostname, 22);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setPassword(ndbcluster_password);
		session.connect();
		Channel channel = (Channel) session.openChannel("exec");
		((ChannelExec) channel).setCommand(ndbcluster_command);
		((ChannelExec) channel).setInputStream(null);
		((ChannelExec) channel).setErrStream(System.err);
		return channel;

	}

	public WebResource client() throws ClassNotFoundException, SQLException {
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			client_url = properties.getProperty("clienturl");

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		Client client = Client.create();
		WebResource webResource = client.resource(client_url);

		return webResource;
	}

	public WebResource client1() throws ClassNotFoundException, SQLException {
		try {

			File file = new File("config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.loadFromXML(fileInput);
			fileInput.close();
			client_url1 = properties.getProperty("clienturl1");

		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		Client client = Client.create();
		WebResource webResource1 = client.resource(client_url1);

		return webResource1;
	}
}
