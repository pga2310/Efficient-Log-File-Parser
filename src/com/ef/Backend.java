package com.ef;
 
import java.io.FileNotFoundException; 
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.util.List;  

import com.mysql.jdbc.Statement;

public class Backend { 
	
	// CONNECT TO THE DATABASE
	static Connection getDbConnect() throws SQLException{
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e1) { 
			e1.printStackTrace();
		}
		
		/**
		 * =================
		 * CONNECT TO YOUR DATABASE: DATABASE_NAME, USER_ID AND PASSWORD 
		 * ================= 
		 */
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=root&password=");
		String createDB = "CREATE DATABASE IF NOT EXISTS Parser";
		Statement statement =(Statement) connection.createStatement(); 
		statement.executeUpdate(createDB);
		return connection;
	}
	
	
	
	// CREATE TABLE TO ADD BLOCK IP ADDRESSES
	void getDbBlocked(List<String> list) throws FileNotFoundException, IOException, SQLException{
		
		// GET CONNECTION
		Connection connection = getDbConnect();
		Statement statement = (Statement) connection.createStatement(); 
		
		 
		// CREATE TABLE 	
		String createTable = "CREATE TABLE IF NOT EXISTS Parser.BLOCKED_IP ("
							+"id int(11) NOT NULL AUTO_INCREMENT," 
							+"ip varchar(255) NOT NULL UNIQUE," 
							+"comments text NOT NULL,"
							+"PRIMARY KEY (`id`)"
							+")";
			
		// TRUNCATE TABLE IF TABLE ALREADY EXISTS
		// REMOVE THIS IF NOT REQUIRED
		String truncateTable = "TRUNCATE TABLE Parser.BLOCKED_IP";
				
		// EXECUTE THE QUERIES
		statement.executeUpdate(createTable);
		System.out.println("Table Blocked Ip address created");
		statement.executeUpdate(truncateTable); 
		 		
		// ITERATE THROUGH THE LIST AND ADD ENTRIES TO TABLE
		for(int i = 0; i < list.size(); i++) {
			String[] logFormat = list.get(i).split("\\|");
			String sql = "INSERT INTO Parser.blocked_ip "
					+ "(ip, comments) " + "VALUES"
					+ "('"+logFormat[0]+"','"+logFormat[1]+"')";
			statement.executeUpdate(sql);
		} 
		
		// PRINT TO CONSOLE WHEN OPERATION IS COMPLETE
		System.out.println("Blocked Ip table ready.");
		
		// CLOSE CONNECTIONS
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) { 
				e.printStackTrace();
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e){ 
				e.printStackTrace();
			}
		}		
	}
	
	
	// LOAD LOG FILE TO THE DATABASE
	void getDbOperation(List<String> list, String path) throws FileNotFoundException, IOException, SQLException{
		
		// GET DATABSE CONNECTION
		Connection connection = getDbConnect();
		Statement statement = (Statement) connection.createStatement();
		
			
		// CREATE TABLE LOG_DATA
		String createTable = "CREATE TABLE IF NOT EXISTS Parser.LOG_DATA ("
							+"date datetime NOT NULL,"
							+"ip varchar(255) NOT NULL,"
							+"request text NOT NULL,"
							+"status varchar(255) NOT NULL,"
							+"user_agent text NOT NULL" 
							+")";
				
		// TRUNCATE TABLE IF ALREADY EXISTS 
		// DO THIS IF REQUIRED
		String truncateTable = "TRUNCATE TABLE Parser.LOG_DATA";
			
		// LOAD DATA TO THE TABLE 
		String sql = "LOAD DATA LOW_PRIORITY LOCAL INFILE"
					+" '"+path+"'"
					+" REPLACE INTO TABLE Parser.LOG_DATA CHARACTER SET latin1"
					+" FIELDS TERMINATED BY '|' LINES TERMINATED BY '\r\n';";
			
		// EXECUTE THE QUERIES
		// AND PRINT TO THE CONSOLE
		statement.executeUpdate(createTable);
		System.out.println("Table Log Data created");
		statement.executeUpdate(truncateTable);
		try{
			statement.executeUpdate(sql);
			System.out.println("Log table ready.");
		}catch(Exception e){
			System.out.println("Error occured\n"+e);
		}
		
		// CLOSE THE DATABASE CONNECTIONS
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) { 
				e.printStackTrace();
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e){ 
				e.printStackTrace();
			}
		}		
	
	}
	
}
