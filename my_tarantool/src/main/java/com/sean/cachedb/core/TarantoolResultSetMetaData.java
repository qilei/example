package com.sean.cachedb.core;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Tarantool 查询的结果集的ResultSetMetaData
 * ps:该ResultSetMetaData的实际内容应该是对应关系数据库的ResultSetMetaData
 *      这样做的目的是让开发者在开发过程中，感觉是在操作mysql，而不是tarantool
 * @author shaojieyue
 * @date 2013-03-28 17:29:03
 */
public class TarantoolResultSetMetaData implements ResultSetMetaData {
	
	/**
	 * 存放 column别名 和 columnName、下标 和columName的对应关系
	 */
	private Map<String,String> columns ;
	
	/**
	 * tarantool space index 和 关系型数据库table的column index 的对应关系
	 * key： 关系型数据库table的columnName
	 * value: tarantool space index
	 */
	private Map<String,Integer> tarRdbMapping;
	
	
	public TarantoolResultSetMetaData(MetaData metaData,Map<String, String> columns) {
		super();
		this.columns=columns;
		tarRdbMapping=metaDataMaping(metaData);
		System.out.println(tarRdbMapping);
	}
	
	/**
	 * 解析tarantool 列下标和table columnName的对应关系
	 * @param metaData 
	 * @return 对应关系 key: table columName value:tarantool 列下标
	 */
	private Map<String,Integer> metaDataMaping(MetaData metaData){
		JSONObject fields = null;
		Iterator<String> iter= null;
		String key = null;
		String[] arr = null;
		String index = null;
		Map<String,Integer> map =null;
		
		try {
			fields = new JSONObject(metaData.getFields());
			map= new HashMap(fields.length());
			iter= fields.keys();
			while(iter.hasNext()){
				key = iter.next();
					arr =  fields.getString(key).split("\\|");
					if(arr.length>0){
						index=arr[0];
					}
					map.put(key, Integer.valueOf(index) );
			}
		} catch (JSONException e) {
			return null;
		}
		return map;
	}
	
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public String getCatalogName(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public String getColumnClassName(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public int getColumnCount() throws SQLException {
		return columns.size();
	}

	public int getColumnDisplaySize(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public String getColumnLabel(int index) throws SQLException {
		return getColumnName(index);
	}
	
	public String getColumnName(int rdbIndex) throws SQLException {
		throw new UnsupportedFeatureException();
	}
	
	/**
	 * 根据RDB sql 语句 查询列对应的index 获取tarantool 对应的 space列的index
	 * @param rdbIndex RDB sql 语句 查询列对应的index
	 * @return  指定space列的index
	 * @throws SQLException
	 */
	public Integer getTarantToolColumnIndex(int rdbIndex) throws SQLException {
		//下标所对应的columnName
		String columnName=columns.get(rdbIndex+"");
		if(columnName==null){
			throw new SQLException("rdbIndex '"+rdbIndex+"' is not exist.");
		}
		Integer tarantoolIndex = (Integer)getMapValueIgnoreCase(tarRdbMapping,columnName);
		if(tarantoolIndex==null){
			throw new SQLException("rdbIndex '"+rdbIndex+"' is not exist.");
		}
		return tarantoolIndex;
	}
	
	/**
	 * 根据RDB sql 语句 查询列对应的columnName 获取tarantool 对应的 space列的index
	 * @param rdbColumnName rdbIndex RDB sql 语句 查询列对应的columnName
	 * @return 指定space列的index
	 * @throws SQLException
	 */
	public Integer getTarantToolColumnIndex(String rdbColumnName) throws SQLException {
		
		if(rdbColumnName==null){
			throw new IllegalArgumentException("rdbColumnName is null .");
		}
		
		//下标所对应的columnName
		String columnName=(String)getMapValueIgnoreCase(columns,rdbColumnName);
		
		if(rdbColumnName==null){
			throw new SQLException("rdbColumnName '"+rdbColumnName+"' is not exist.");
		}
		//columnName对应的tarantool 下标
		Integer tarantoolIndex = (Integer)getMapValueIgnoreCase(tarRdbMapping,columnName);
		if(tarantoolIndex==null){
			throw new SQLException("tarantoll conf error '"+columnName+"' is not conf.");
		}
		
		return tarantoolIndex;
	}
	
	/**
	 * 通过key获取map值，key不区分大小写
	 * @param map
	 * @param key
	 * @return
	 */
	private Object getMapValueIgnoreCase(Map map,String key){
		Object val=null;
		val=map.get(key.toLowerCase());
		if(val==null){
			//试着转为大写查找
			val=map.get(key.toUpperCase());
		}
		return val;
	}
	

	public int getColumnType(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public String getColumnTypeName(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public int getPrecision(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public int getScale(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public String getSchemaName(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public String getTableName(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean isAutoIncrement(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean isCaseSensitive(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean isCurrency(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean isDefinitelyWritable(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public int isNullable(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean isReadOnly(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean isSearchable(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean isSigned(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean isWritable(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

}
