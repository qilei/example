package com.sean.cachedb.core;

import java.sql.Connection;
import java.util.logging.Logger;

public class TestConnection {
	
	private static Logger log = Logger.getLogger(TestConnection.class.getName());
	
	private static String host = CONFIG.TARAN_LOCAL;
	private static int port = CONFIG.TARAN_PORT;
	
	public static void main(String[] args) throws Exception {
		CacheDriver driver = new CacheDriver();
		Connection conn = driver.connect("jdbc:tarantool://" + host + ":" + port);
		log.info("连接是否关闭？" + conn.isClosed());
	}
}
