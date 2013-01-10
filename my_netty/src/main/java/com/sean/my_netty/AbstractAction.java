package com.sean.my_netty;

import org.jboss.netty.handler.codec.http.HttpRequest;

public abstract class AbstractAction {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public AbstractAction(String name) {
		this.name = name;
	}
	
	public abstract String action(HttpRequest request);
}
