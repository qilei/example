package com.sean.my_tarantool;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.tarantool.core.Tuple;

public class TestMyTarantool {
	
	private static Logger log = Logger.getLogger(TestMyTarantool.class.getName());
	
	public static void main(String[] args) throws Exception {
		TestMyTarantool t = new TestMyTarantool();
		
//		int count = 100;
//		t.testAddUser(count);
//		t.testReplaceUser(count);
//		t.testDeleteUser(count);
//		t.testQueryUser(count);
		t.testQueryUser(1, 1, 2);
		
		
//		MetaData data = new MetaData();
//		data.setTablename("users");
//		data.setSpace(1);
//		
//		Map<String, String> fields = new LinkedHashMap<String, String>();
//		fields.put("userid", "0|"+CONFIG.TYPE_INT);
//		fields.put("username", "1|"+CONFIG.TYPE_STR);
//		fields.put("sex", "2|"+CONFIG.TYPE_BOOL);
//		fields.put("age", "3|"+CONFIG.TYPE_INT);
//		JSONObject jsonObject = new JSONObject(fields);
//		data.setFields(jsonObject.toString());
////
//		t.testAddMetaData(data);
//		t.testDelMetaData(data.getTablename());
//		t.testFindMetaData(data.getTablename());
		
	}
	
	//连接测试
	public void testConnection() {
		Connection conn = new Connection("10.1.156.206", 33013);
		log.info("Connection's ping status: " + conn.isOK());
	}
	
	//在space = 0 中添加元数据信息	 
	public void testAddMetaData(MetaData data) {
		Connection conn = new Connection("10.1.156.206", 33013);
		
		MetaData savedData = conn.addMetaData(data);
		 
		if(savedData != null) {
			log.info("add data success!");
		}
	}
	
	public void testFindMetaData(String tablename) {
		Connection conn = new Connection("10.1.156.206", 33013);
		MetaData data = conn.queryTableInfo(tablename);
		 
		if(data != null) {
			log.info("the information of table " + tablename);
			StringBuffer sb = new StringBuffer("Field\tValue");
			
			sb.append("\rtablename\t" + data.getTablename());
			sb.append("\rspace\t" + data.getSpace());
			sb.append("\rfields\t" + data.getFields());

		}else{
			log.info("Not find meta data!");
		}
	}
	
	public void testDelMetaData(String tablename) {		
		Connection conn = new Connection("10.1.156.206", 33013);
		int r = conn.delMetaData(tablename);
		log.info("delete is ok ? " + r);
	}
	
	public void testAddUser(int count) {
		Connection conn = new Connection("10.1.156.206", 33013);
		
		Tuple tuple = null;
		for(int i=1; i<=count; i++) {
			tuple = new Tuple(4).setInt(0, i).setString(1, "user-"+i, CONFIG.DEFAULT_ENCODE).setBoolean(2, i%2==0).setInt(3, new Random().nextInt(100));
			Integer r = conn.insert(1, tuple);
			log.info("Tuple"+i+" insert ok ? " + r);
		}
	}
	
	public void testReplaceUser(int count) {
		Connection conn = new Connection("10.1.156.206", 33013);
		
		Tuple tuple = null;
		for(int i=1; i<=count; i++) {
			tuple = new Tuple(4).setInt(0, i).setString(1, "user-"+i, CONFIG.DEFAULT_ENCODE).setBoolean(2, i%2==0).setInt(3, new Random().nextInt(100));
			Integer r = conn.insertOrReplace(1, tuple);
			log.info("Tuple"+i+" replace ok ? " + r);
		}
	}
	
	public void testQueryUser(int range) {
		Connection conn = new Connection("10.1.156.206", 33013);
		
		Random random = new Random();
		Integer random_id = random.nextInt(range);
		Tuple tuple = new Tuple(1).setInt(0, random_id);
		
		Tuple _tuple = conn.queryOne(1, 0, 0, tuple);
		
		if(_tuple != null) {
			log.info("user=" +random_id+ ", username is " + _tuple.getString(1, CONFIG.DEFAULT_ENCODE) + ", sex = "+(_tuple.getBoolean(2) ? "man" : "woman")+", age = " + _tuple.getInt(3));
		}else{
			log.info("user=" + random_id + " not be found!");
		}
	}
	
	public void testQueryUser(int uid, int offset, int limit) {
		Connection conn = new Connection("10.1.156.206", 33013);
		
		Tuple tuple = new Tuple(1).setInt(0, uid);
		List<Tuple> tuples = conn.query(1, 0, offset, limit, tuple);
		
		if(tuples != null && tuples.size() > 0) {
			for(Tuple _tuple : tuples) {
				log.info("user=" +_tuple.getInt(0)+ ", username is " + _tuple.getString(1, CONFIG.DEFAULT_ENCODE) + ", sex = "+(_tuple.getBoolean(2) ? "man" : "woman")+", age = " + _tuple.getInt(3));
			}
		}else{
			log.info("Not found users!");
		}
	}
	
	public void testDeleteUser(int count) {
		Connection conn = new Connection("10.1.156.206", 33013);
		Tuple tuple = null;
		for(int i=1; i<=count; i++) {
			tuple = new Tuple(1).setInt(0, i);
			Integer r = conn.delete(1, tuple);
			log.info("Tuple"+i+" delete ok ? " + r);
		}
	}
}
