package com.sean.my_redis;

import redis.clients.jedis.Jedis;

public class RedisTest {
	
	private final static String HOST = "192.168.79.128";
	private final static int PORT = 6379;
	
	private final static int num = 100000;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Jedis jedis = new Jedis(HOST, PORT);
		
		jedis.flushDB();
		
//		String status = jedis.set("foo", "bar");
//		System.out.println("foo = bar is set into redis : " + status);
//		
//		String value = jedis.get("foo");
//		System.out.println("the value of the key 'foo' is : " + value);
//		
//		status = jedis.set("foo", "new _bar");
//		System.out.println("foo = new_bar is set into redis : " + status);
//		
//		value = jedis.get("foo");
//		System.out.println("the new value of the key 'foo' is : " + value);
//		
//		Set<String> keySet = jedis.keys("?x");
//		System.out.println("All keys in redis : ");
//		for(String k : keySet) {
//			System.out.print(k + " ");
//		}
//		System.out.println();
		
		//压力测试
		long start = System.currentTimeMillis();
		for(int i = 0; i < num; i++) {
			jedis.set("key"+i, "value"+i);
		}
		System.out.println("Set " +num+" key-values cost time " + (System.currentTimeMillis()-start) + "ms.");
		
		start = System.currentTimeMillis();
		for(int i = 0; i < num; i++) {
			jedis.get("key"+i);
		}
		System.out.println("Get " +num+" key-values cost time " + (System.currentTimeMillis()-start) + "ms.");
		
	}

}
