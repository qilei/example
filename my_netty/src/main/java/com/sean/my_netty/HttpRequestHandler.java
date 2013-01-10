package com.sean.my_netty;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

public class HttpRequestHandler extends SimpleChannelUpstreamHandler {
	private HttpRequest request; 
    private boolean readingChunks; 
    
    public String getHost(HttpRequest req, String defaultName) {
    	String host = req.getHeader("HOST");
    	return host==null ? defaultName : host;
    }
    
    @Override 
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception { 
    	StringBuffer buf = new StringBuffer();
    	
    	if (!readingChunks) { 
            this.request = (HttpRequest) e.getMessage(); 
            
            buf.setLength(0); 
//            buf.append("WELCOME TO THE WILD WEB SERVER\r\n"); 
//            buf.append("===================================\r\n"); 
//  
//            buf.append("VERSION: " + request.getProtocolVersion() + "\r\n"); 
//            buf.append("HOSTNAME: " + getHost(request, "unknown") + "\r\n"); 
            String uri = request.getUri();
//            buf.append("REQUEST_URI: " + uri + "\r\n\r\n"); 
            
            String actionName = StringUtils.substring(uri, 1, uri.indexOf("?"));
//            buf.append("Action_Name: " + actionName + "\r\n\r\n");
            
            AbstractAction action = Configure.container.get(actionName);
            if(action != null) {
            	buf.append(action.action(request) + "\r\n\r\n");
            }
  
//            for (Map.Entry<String, String> h: request.getHeaders()) { 
//                buf.append("HEADER: " + h.getKey() + " = " + h.getValue() + "\r\n");
//            } 
//            buf.append("\r\n"); 
  
//            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri()); 
//            Map<String, List<String>> params = queryStringDecoder.getParameters(); 
//            if (!params.isEmpty()) { 
//                for (Entry<String, List<String>> p: params.entrySet()) { 
//                    String key = p.getKey(); 
//                    List<String> vals = p.getValue(); 
//                    for (String val : vals) { 
//                        buf.append("PARAM: " + key + " = " + val + "\r\n"); 
//                    } 
//                } 
//                buf.append("\r\n"); 
//            } 
  
            if (request.isChunked()) { 
                readingChunks = true; 
            } else { 
                ChannelBuffer content = request.getContent(); 
               if (content.readable()) { 
                   buf.append("CONTENT: " + content.toString(CharsetUtil.UTF_8) + "\r\n"); 
               } 
               writeResponse(e,buf); 
           } 
       } else { 
           HttpChunk chunk = (HttpChunk) e.getMessage(); 
           if (chunk.isLast()) { 
               readingChunks = false; 
               buf.append("END OF CONTENT\r\n"); 
 
               HttpChunkTrailer trailer = (HttpChunkTrailer) chunk; 
               if (!trailer.getHeaderNames().isEmpty()) { 
                   buf.append("\r\n"); 
                   for (String name: trailer.getHeaderNames()) { 
                       for (String value: trailer.getHeaders(name)) { 
                           buf.append("TRAILING HEADER: " + name + " = " + value + "\r\n"); 
                       } 
                   } 
                   buf.append("\r\n"); 
               } 
 
               writeResponse(e,buf); 
           } else { 
               buf.append("CHUNK: " + chunk.getContent().toString(CharsetUtil.UTF_8) + "\r\n"); 
           } 
       } 
   } 
   
   public boolean isKeepAlive(HttpRequest req) {
	   String status = req.getHeader(HttpHeaders.Names.CONNECTION);
	   if(status != null && status.equals("keep-alive")) {
		   return true;
	   }else{
		   return false;
	   }
   }
   
   private void writeResponse(MessageEvent e, StringBuffer buf) { 
       // Decide whether to close the connection or not. 
       boolean keepAlive = isKeepAlive(request); 
       
       send100Continue(e); 

       // Build the response object. 
       HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK); 
       response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8)); 
       response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8"); 
 
       if (keepAlive) { 
           // Add 'Content-Length' header only for a keep-alive connection. 
           response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, response.getContent().readableBytes()); 
       } 
 
       // Encode the cookie. 
       String cookieString = request.getHeader(HttpHeaders.Names.COOKIE); 
       if (cookieString != null) { 
           CookieDecoder cookieDecoder = new CookieDecoder(); 
           Set<Cookie> cookies = cookieDecoder.decode(cookieString); 
           if(!cookies.isEmpty()) { 
               // Reset the cookies if necessary. 
               CookieEncoder cookieEncoder = new CookieEncoder(true); 
               for (Cookie cookie : cookies) { 
                   cookieEncoder.addCookie(cookie); 
               } 
               response.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode()); 
           } 
       } 
 
       // Write the response. 
       ChannelFuture future = e.getChannel().write(response); 
 
       // Close the non-keep-alive connection after the write operation is done. 
       if (!keepAlive) { 
           future.addListener(ChannelFutureListener.CLOSE); 
       } 
   } 
 
   private void send100Continue(MessageEvent e) { 
       HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE); 
       e.getChannel().write(response); 
   } 
 
   @Override 
   public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) 
           throws Exception { 
       e.getCause().printStackTrace(); 
       e.getChannel().close(); 
   } 
}
