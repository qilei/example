package com.sean.my_mqtt;

import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * MQTT Subscriber
 * @author huixiao200068
 *
 */
public class MQTTSub {
	public static String doTest() {    
        try {    
           
        	//创建MqttClient   
            MqttClient client = new MqttClient(Configuration.TCPAddress, "scriber"); 
            
            //回调处理类   
            SubCallBack callback = new SubCallBack("scriber");    
            client.setCallback(callback);
            
            //创建连接可选项信息   
            MqttConnectOptions conOptions = new MqttConnectOptions();    
            conOptions.setCleanSession(Configuration.cleanSession); 
            conOptions.setKeepAliveInterval(Configuration.keepAliveInterval);
            
            //连接broker   
            client.connect(conOptions);

            //发布相关的订阅   
            client.subscribe(Configuration.topic, Configuration.QoS);    
            
            Scanner scan = new Scanner(System.in);
            for(String input="";!input.equalsIgnoreCase("q");input=scan.nextLine());
            
            client.disconnect(); 
            
            System.out.println("Finished");
        } catch (Exception e) {    
            e.printStackTrace();    
            return "failed";    
        }    
        return "success";    
    }
	
	public static void main(String[] args) throws Exception {
		doTest();
	}
}
