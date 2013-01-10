package com.sean.my_kafka;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.javaapi.producer.ProducerData;
import kafka.producer.ProducerConfig;

public class TestProducer {
	
	private static Producer<String, String> producer = null;
	
	static {
		Properties prop = new Properties();
		prop.put("zk.connect", "localhost:2181");
		prop.put("serializer.class", "kafka.serializer.StringEncoder");
		ProducerConfig config = new ProducerConfig(prop);
		producer = new Producer<String, String>(config);
	}
	
	public static void main(String[] args) throws Exception {
		String message = "";
		if(args != null && args.length > 0) message = args[0];
		ProducerData<String, String> data = new ProducerData<String, String>("test", message);
		producer.send(data);
	}

}
