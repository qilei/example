package com.sean.my_kafka;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import kafka.api.FetchRequest;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.Message;
import kafka.message.MessageAndOffset;

public class TestConsumer {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		SimpleConsumer consumer = new SimpleConsumer("127.0.0.1", 9092, 10000, 1024000);

		long offset = 0;
		while (true) {
		  // create a fetch request for topic “test”, partition 0, current offset, and fetch size of 1MB
		  FetchRequest fetchRequest = new FetchRequest("test", 0, offset, 1000000);

		  // get the message set from the consumer and print them out
		  ByteBufferMessageSet messages = consumer.fetch(fetchRequest);
		  for(MessageAndOffset msg : messages) {
			  Message message = msg.message();
			  System.out.println("consumed: " + toString(message.payload()));
			  // advance the offset after consuming each message
			  offset = msg.offset();
		  }
		}


	}
	
	public static String toString(ByteBuffer buf) throws Exception {
		CharsetDecoder decoder = null;
		CharBuffer charBuf = null;
		
		try{
			decoder = Charset.forName("utf-8").newDecoder();
			charBuf = decoder.decode(buf);
			return charBuf.toString();
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}

}
