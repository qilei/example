package com.sean.my_netty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class HttpServer {

	private final static Logger log = Logger.getLogger(HttpServer.class.getSimpleName());
	private final static int PORT = 8082;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if(args == null || args.length < 1) {
			start(PORT);
		} else {
			String port = StringUtils.trimToEmpty(args[0]);
			if(StringUtils.isNotBlank(port) && StringUtils.isNumeric(port)) {
				start(Integer.parseInt(port));
			}else {
				start(PORT);
			}
			
		}
	}
	
	public static void start(int port) throws Exception {
		
		init();
		
		// Configure the server.         
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), 
                        Executors.newCachedThreadPool()));
        
        // Set up the event pipeline factory. 
        bootstrap.setPipelineFactory(new HttpServerPipelineFactory()); 
 
        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));
        
        log.info("Server on "+InetAddress.getLocalHost().getHostAddress().toString()+":"+port+" has started.");
	}
	
	public static void init() throws Exception {
		addAction(new SimpleAction());
	}
	
	public static void addAction(AbstractAction action) {
		Configure.container.put(action.getName(), action);
	}

}
