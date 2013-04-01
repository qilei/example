package com.sean.cachedb.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

public class TestQuery {
	
	private static Logger log = Logger.getLogger(TestQuery.class.getName());
	
	private static String host = CONFIG.TARAN_LOCAL;
	private static int port = CONFIG.TARAN_PORT;
	
	public static void main(String[] args) throws Exception {
		CacheDriver driver = new CacheDriver();
		Connection conn = driver.connect("jdbc:tarantool://" + host + ":" + port);
		
		String sql = "select * from users where userid = " + 60;
//		String sql = "select username uname, age from users where userid = " + 56;
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(sql);
		if(rs != null) {
			while(rs.next()) {
//				log.info("username="+rs.getString(1)+",  age=" + rs.getInt(2));
//              log.info("username="+rs.getString("uname")+",  age=" + rs.getInt("age"));
				log.info("userid="+rs.getInt(1)+", username="+rs.getString(2)+", sex="+rs.getBoolean(3)+", age=" + rs.getInt(4));
				log.info("userid="+rs.getInt("userid")+", username="+rs.getString("username")+", sex="+rs.getBoolean("sex")+", age=" + rs.getInt("age"));
			}
		}
	}
}
