package com.sean.my_tarantool;

import java.util.List;

import org.tarantool.core.TarantoolConnection;
import org.tarantool.core.Tuple;
import org.tarantool.facade.TarantoolTemplate;
import org.tarantool.pool.SocketChannelPooledConnectionFactory;

public class Connection {
	
	private static SocketChannelPooledConnectionFactory factory;
	private static TarantoolConnection connection;
	private static TarantoolTemplate template;
	
	public Connection() {
		this(CONFIG.TARAN_LOCAL, CONFIG.TARAN_PORT);
	}
	
	public Connection(String host, Integer port) {
		if(factory == null) {
			factory = new SocketChannelPooledConnectionFactory(host, port, CONFIG.POOL_MIN, CONFIG.POOL_MAX);
		}
		
		if(connection == null) {
			connection = factory.getConnection();
		}
		
		if(template == null) {
			template = new TarantoolTemplate(factory);
		}
	}
	
	public Boolean isOK() {
		return connection.ping();
	}
	
	public void close() {
		connection.close();
	}
	
	public Integer insert(int space, Tuple tuple) {
		return connection.insert(space, tuple);
	}
	
	public Integer insertOrReplace(int space, Tuple tuple) {
		return connection.insertOrReplace(space, tuple);
	}
	
	public Tuple queryOne(int space, int index, int offset, Tuple... keys) {
		return connection.findOne(space, index, offset, keys);
	}
	
	public List<Tuple> query(int space, int index, int offset, int limit, Tuple... keys) {
		return connection.find(space, index, offset, limit, keys);
	}
	
	public Integer delete(int space, Tuple tuple) {
		return connection.delete(space, tuple);
	}

	public MetaData queryTableInfo(String tablename) {
		TarantoolTemplate template = new TarantoolTemplate(factory);
		List<MetaData> users = template.find(MetaData.class, 0, "tablename").condition(tablename).list();
		if(users != null && users.size() > 0) {
			return users.get(0);
		}
		return null;
	}
	
	public MetaData addMetaData(MetaData data) {
		return template.save(data).insertOrReplaceAndGet();
	}
	
	public int delMetaData(String tablename) {
		return template.delete(MetaData.class, tablename).delete();
	}
	
	public MetaData delAndGetMetaData(String tablename) {
		return template.delete(MetaData.class, tablename).deleteAndGet();
	}

}
