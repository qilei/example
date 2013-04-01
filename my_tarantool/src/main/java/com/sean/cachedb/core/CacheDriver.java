package com.sean.cachedb.core;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import org.tarantool.facade.TarantoolTemplate;
import org.tarantool.pool.SocketChannelPooledConnectionFactory;

public class CacheDriver implements Driver{
	
	private static Logger log = Logger.getLogger(CacheDriver.class.getName());
	
	private final static String JDBC_PROTOCOL = "jdbc:tarantool";
	private final static String JDBC_PROTOCOL_SEPARATOR = ":";
	private final static String JDBC_PROTOCOL_SUFFIX = JDBC_PROTOCOL_SEPARATOR + "//";
	
	private final static DriverPropertyInfo[] EMPTY_INFO = new DriverPropertyInfo[0];
	private final static int MAJOR_VERSION = 1;
    private final static int MINOR_VERSION = 0;
    
    private final static int POOL_MIN = 1;
    private final static int POOL_MAX = 15;
    
    private static String JDBC_HOST;
    private static int    JDBC_PORT;
    
    private static SocketChannelPooledConnectionFactory connectionFactory;
	
    public static final CacheDriver INSTANCE;
    static {
        try {
            DriverManager.registerDriver( INSTANCE = new CacheDriver() );
        } catch (SQLException e) {
            throw new IllegalStateException("Untable to register " + CacheDriver.class.getName() + ": "+ e.getMessage());
        }
    }
    
    public Connection connect(String url) throws Exception {
    	return connect(url, null);
    }
    
	public Connection connect(String url, Properties info) throws SQLException {
		if (!acceptsURL(url)) {
			log.info("The connect url is not correct.");
            return null;
        }
		
		if(connectionFactory == null) {
			connectionFactory = new SocketChannelPooledConnectionFactory(JDBC_HOST, JDBC_PORT, POOL_MIN, POOL_MAX);
		}
		
		return new CacheConnection(JDBC_HOST, JDBC_PORT, connectionFactory.getConnection(), new TarantoolTemplate(connectionFactory));
	}

	public boolean acceptsURL(String url) throws SQLException {
		if(url.startsWith(JDBC_PROTOCOL)) {
			int indexOfSuffix = url.indexOf(JDBC_PROTOCOL_SUFFIX); 
			if(indexOfSuffix != -1 && JDBC_PROTOCOL.length() == indexOfSuffix) {
				String urlAndPort = url.substring(indexOfSuffix + JDBC_PROTOCOL_SUFFIX.length());
				String[] connInfo = urlAndPort.split(JDBC_PROTOCOL_SEPARATOR);
				if(connInfo.length == 2) {
					JDBC_HOST = connInfo[0];
					JDBC_PORT = Integer.parseInt(connInfo[1].trim());
					return true;
				}
			}
		}
		return false;
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		return EMPTY_INFO;
	}

	public int getMajorVersion() {
		return MAJOR_VERSION;
	}

	public int getMinorVersion() {
		return MINOR_VERSION;
	}

	public boolean jdbcCompliant() {
		return false;
	}

}
