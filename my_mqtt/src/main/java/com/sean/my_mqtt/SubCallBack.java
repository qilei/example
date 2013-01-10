package com.sean.my_mqtt;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class SubCallBack implements MqttCallback {
	
	private String intanceName = "";
	
	public SubCallBack(String instanceName) {this.intanceName = instanceName;}
	
	public void connectionLost(Throwable cause) {
		System.out.println("Connection lost : " + cause.getMessage() + "|" + ((MqttException)cause).getReasonCode() + "|" + cause.getCause());
		cause.printStackTrace();
	}

	public void messageArrived(MqttTopic topic, MqttMessage message)
			throws Exception {
		try {    
            System.out.println("Message arrived : " + message.toString() + " on Topic : " + topic.getName() + " for instance : " + this.intanceName);   
        } catch (Exception e) {    
            e.printStackTrace();    
        } 
		
	}

	public void deliveryComplete(MqttDeliveryToken token) {
		System.out.println("Delivery token " + token.hashCode() + " has been received : " + token.isComplete());
	}
	
}
