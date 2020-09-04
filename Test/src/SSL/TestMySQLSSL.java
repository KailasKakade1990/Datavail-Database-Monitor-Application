package SSL;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestMySQLSSL {
	public static void main(String[] args) {

		System.out.println("Printed:::");
		TestMySQLSSL.javaMYSQLConnect();
		System.out.println("Printed:::");
	}

	public static void javaMYSQLConnect() {

		Connection con = null;
		try {
			String url = "jdbc:mysql://localhost:3306" + "?verifyServerCertificate=false" + "&useSSL=true"
					+ "&requireSSL=true";
			String user = "delta";
			String password = "root";

			Class dbDriver = Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, user, password);
			System.out.println("connected:::");

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error:::");
		} finally {
			if (con != null) {
				try {

					con.close();
				} catch (Exception e) {
				}
			}
		}
	}

}
