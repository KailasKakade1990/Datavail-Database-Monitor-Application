package SSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

class MysqlCon {
	public static void main(String args[]) {
		String uname = "root";
		String password = "root";
		try {

			// mysql -u delta -p -h localhost --ssl-ca=~/delta-ssl/ca.pem
			// --ssl-cert=~/delta-ssl/client-cert.pem
			// --ssl-key=~/delta-ssl/client-key.pem

			System.setProperty("javax.net.ssl.keyStore", "~/delta-ssl/client-key.pem");
			System.setProperty("javax.net.ssl.keyStorePassword", "password");
			System.setProperty("javax.net.ssl.trustStore", "path_to_truststore_file");
			System.setProperty("javax.net.ssl.trustStorePassword", "password");

			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/", uname, password);
			// here sonoo is database name, root is username and password

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select 1 from dual");
			while (rs.next())
				System.out.println(rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3));
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}