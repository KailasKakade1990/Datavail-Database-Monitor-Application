
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDBConnect {

	public static void main(String[] args) throws SQLException {

		try {
			String dbURL = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=whatEverYourHostNameIs)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=yourServiceName)))";
			String strUserID = "yourUserId";
			String strPassword = "yourPassword";
			Connection myConnection = DriverManager.getConnection(dbURL, strUserID, strPassword);

			Statement sqlStatement = myConnection.createStatement();
			String readRecordSQL = "select * from sa_work_order where WORK_ORDER_NO = '1503090' ";
			ResultSet myResultSet = sqlStatement.executeQuery(readRecordSQL);
			while (myResultSet.next()) {
				System.out.println("Record values: " + myResultSet.getString("WORK_ORDER_NO"));
			}
			myResultSet.close();
			myConnection.close();

		} catch (Exception e) {
			System.out.println(e);
		}
	}
}