package com.sean.my_memcached;

import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

public class MemcacheTest {
	
	private final static String URI = "192.168.79.128:11211";
	
	private final static int num = 100000;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		MemcachedClient client = null;
		try {
			client = new MemcachedClient(AddrUtil.getAddresses(URI));
			
			//压力测试
			long start = System.currentTimeMillis();
			for(int i = 0; i < num; i++) {
				client.set("key"+i, 60, "value"+i);
			}
			System.out.println("Set " +num+" key-values cost time " + (System.currentTimeMillis()-start) + "ms.");
			
			start = System.currentTimeMillis();
			for(int i = 0; i < num; i++) {
				client.get("key"+i);
			}
			System.out.println("Get " +num+" key-values cost time " + (System.currentTimeMillis()-start) + "ms.");
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(client != null) {
				client.shutdown(3, TimeUnit.SECONDS);
			}
		}
	}

}
