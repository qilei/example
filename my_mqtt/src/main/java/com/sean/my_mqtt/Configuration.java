package com.sean.my_mqtt;

/**
 * Configuration for MQTT publish/subscribe
 * @author huixiao200068
 *
 */

public final class Configuration {
	
	public static final String TCPAddress = System.getProperty("TCPAddress","tcp://10.1.36.134:1883");
	
	public static final String topic = System.getProperty("topic", "routes/test");
	
	public static final String publication = System.getProperty("publication", System.currentTimeMillis() + ", Current Timestamp。");
	
	public static final int sleepTimeout = Integer.parseInt(System.getProperty("sleepTimeout", "1000"));
	
	public static final boolean cleanSession = Boolean.parseBoolean(System.getProperty("cleanSession", "false"));
	
	public static final int keepAliveInterval = Integer.parseInt(System.getProperty("keepAliveInterval", "20"));
	
	public static final int QoS = Integer.parseInt(System.getProperty("QoS", "1"));
	
	public static final boolean retained = Boolean.parseBoolean(System.getProperty("retained", "false"));
	
}
