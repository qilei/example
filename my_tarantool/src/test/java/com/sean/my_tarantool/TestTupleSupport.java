package com.sean.my_tarantool;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.tarantool.core.Tuple;
import org.tarantool.facade.TupleSupport;

public class TestTupleSupport {
	
	private static Logger log = Logger.getLogger(TestTupleSupport.class.getName());
	
	public static void main(String[] args) throws Exception {

		TupleSupport tupleSupport = new TupleSupport();
		
		
		Map<String, Integer> fields = new LinkedHashMap<String, Integer>();
		fields.put("userid", 0);
		fields.put("username", 1);
		JSONObject jsonObject = new JSONObject(fields);
		
		Tuple johnSmith = tupleSupport.create("users", 1, jsonObject.toString());

		Object[] array = tupleSupport.parse(johnSmith, new Class<?>[]{String.class, Integer.class, String.class});
		
		if(array != null) {
			log.info("object's num is " + array.length);
			for(Object obj : array) {
				log.info(obj.toString());
			}
		}
	}

}
