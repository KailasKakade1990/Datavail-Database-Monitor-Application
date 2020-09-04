package SSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SSLConnection_MYSQL {

	public static void main(String[] args) throws SQLException {

		
		
		String username = "delta";
		String password = "root";

		StringBuilder url = new StringBuilder();
		url.append("jdbc:mysql://[SERVER]/[SCHEMA]?").append("useSSL=true&").append("requireSSL=true&");

		Connection conn = DriverManager.getConnection(url.toString(), username, password);

		
		System.setProperty("javax.net.ssl.keyStore", "path_to_keystore_file");
		System.setProperty("javax.net.ssl.keyStorePassword", "password");
		System.setProperty("javax.net.ssl.trustStore", "path_to_truststore_file");
		System.setProperty("javax.net.ssl.trustStorePassword", "password");

	}

}
