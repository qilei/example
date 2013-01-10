package com.sean.my_mqtt;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/**
 * MQTT Publisher
 * @author huixiao200068
 *
 */

public class MQTTPub {
	
	public static void doTest(){    
        try {    
            MqttClient client = new MqttClient("tcp://10.1.36.134:1883","mqttserver-pub");    
            MqttTopic topic = client.getTopic("tokudu/china");    
//            MqttMessage message = new MqttMessage("Hello World. Hello IBM".getBytes());    
//            message.setQos(1);    
            client.connect(); 
            
           MqttMessage message = null;
            for(int i=10; i<20; i++) {
            	message = new MqttMessage(("This is message " + i + ", hello!").getBytes());
            	message.setQos(1);
            	MqttDeliveryToken token = topic.publish(message);    
                while (!token.isComplete()){    
                    token.waitForCompletion(1000);    
                }
            }
            
            /*while(true){   
                MqttDeliveryToken token = topic.publish(message);    
                while (!token.isComplete()){    
                    token.waitForCompletion(1000);    
                }    
            }*/ 
            
            client.disconnect();
        } catch (Exception e) {    
            e.printStackTrace();    
        }    
    }
	
	
	public static void main(String[] args) throws Exception {
		try{
			
			MqttClient client = new MqttClient(Configuration.TCPAddress, "publisher");
			
			client.connect();
			
			MqttTopic topic = client.getTopic(Configuration.topic);	
			
			BufferedReader br = null;new BufferedReader(new InputStreamReader(System.in));
			String message = "You don't publish any message!";
			while(true) {
				System.out.print("请输入待发布信息：");
				br = new BufferedReader(new InputStreamReader(System.in));
				String m = br.readLine();
				if(m != null && !m.trim().equals("")){
					message = m;
				}
				
				MqttMessage _m = new MqttMessage(message.getBytes("utf8"));
				_m.setQos(Configuration.QoS);
				
				MqttDeliveryToken token = topic.publish(_m);
                token.waitForCompletion(Configuration.sleepTimeout); 
                
                System.out.println("Delivery token " + token.hashCode() + " has been received : " + token.isComplete());
                
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
