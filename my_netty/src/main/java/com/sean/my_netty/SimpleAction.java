package com.sean.my_netty;

import java.util.List;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

public class SimpleAction extends AbstractAction {
	
	public SimpleAction() {
		super("simpleAction");
	}

	@Override
	public String action(HttpRequest request) {	
		String result = "";
		try{
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri()); 
	        Map<String, List<String>> params = queryStringDecoder.getParameters();
	        
	        if (!params.isEmpty()) { 
	        	List<String> names = params.get("name");
	            result = "Hello," + names.get(0) + "! This is a simple action.";
	        }
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
