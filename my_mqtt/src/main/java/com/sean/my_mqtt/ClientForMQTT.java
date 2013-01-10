package com.sean.my_mqtt;

import java.util.Random;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/**
 * MQTT Client for publish/subscribe
 * @author huixiao200068
 *
 */
public class ClientForMQTT {
	
	private static final String URI = "tcp://10.1.36.134:1883";
	
	private MqttClient client;
	private MqttTopic mqttTopic;
	
	public ClientForMQTT() throws MqttException {
		client = new MqttClient(URI, genClientId(10));
	}
	
	public ClientForMQTT(String uri, String clientId) throws MqttException {
		client = new MqttClient(uri, clientId);
	}
	
	public void connect() throws MqttSecurityException, MqttException {
		client.connect();
	}
	
	public void connect (MqttConnectOptions options) throws MqttSecurityException, MqttException {
		client.connect(options);
	}
	
	public boolean isConnected() {
		return client.isConnected();
	}
	
	public void disconnect() throws MqttException {
		client.disconnect();
	}
	
	public void disconnect(long timeout) throws MqttException {
		client.disconnect(timeout);
	}
	
	public void setCallback(MqttCallback callback) throws MqttException {
		client.setCallback(callback);
	}
	
	public MqttDeliveryToken publish(String topic, MqttMessage message) throws MqttException {
		mqttTopic = client.getTopic(topic);
		return mqttTopic.publish(message);
	}
	
	public MqttDeliveryToken publish(String topic, byte[] payload, int qos, boolean retained) throws MqttException {
		mqttTopic = client.getTopic(topic);
		return mqttTopic.publish(payload, qos, retained);
	}
	
	public void subscribe(String topic, int qos) throws MqttException {
		client.subscribe(topic, qos);
	}
	
	public void unsubscribe(String topic) throws MqttException {
		client.unsubscribe(topic);
	}
	
	public MqttTopic getTopic(String topic) {
		return client.getTopic(topic);
	}
	
	public String getUri() {
		return client.getServerURI();
	}

	public String getClientId() {
		return client.getClientId();
	}

	public static String genClientId(int length) {
	    String base = "abcdefghijklmnopqrstuvwxyz0123456789";   
	    Random random = new Random();   
	    StringBuffer sb = new StringBuffer();   
	    for (int i = 0; i < length; i++) {   
	        int number = random.nextInt(base.length());   
	        sb.append(base.charAt(number));   
	    }   
	    return sb.toString();   
	 } 
	
}
