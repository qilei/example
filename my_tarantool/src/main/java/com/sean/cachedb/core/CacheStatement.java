package com.sean.cachedb.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.tarantool.core.Tuple;

public class CacheStatement implements Statement{
	
	private static Logger log = Logger.getLogger(CacheStatement.class.getName());
	
	private Connection connection;
	private CacheConnection cacheConnection;
	
	private String table;
    private Map<String, String> fields;
    private Map<String, Object> conditions;
    private Map<String, Object> paramValues;
    private int bindCount;

    private MetaData tableInfo;
    private String unique_index_column;
	
	public CacheStatement(Connection conn) {
		this.connection = conn;
		cacheConnection = (CacheConnection) conn;
	}
	
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		try{
			
			CacheSQLParser parser = new CacheSQLParser(connection, sql).parseQuery();
			parseQ(parser, sql);
			
			return runQ();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void parseQ(CacheSQLParser parser, String sql) throws Exception {
		
		
        table = parser.getTableName();
        if(table == null) throw new Exception("Please specify the table name to be search.");

        tableInfo = parser.queryTableInfo(table);
        if(tableInfo == null) throw new Exception("table '"+table+"' is not exist.");

        fields = parser.getSelectFields(tableInfo);
        if(fields == null) throw new Exception("Please specify the fields to be search.");

        conditions = parser.getWhere(tableInfo);
        if(conditions == null) throw new UnSupportException("Please specify query condition in where clause.");
        
        bindCount = parser.getBindCount();
	}
	
	private ResultSet runQ() throws Exception {
        JSONObject fields = new JSONObject(tableInfo.getFields());

        Tuple tuple = new Tuple(conditions.size());

        for(Map.Entry<String, Object> entry : conditions.entrySet()) {
            String fieldInfo = fields.getString(entry.getKey());
            setValue(tuple, fieldInfo, entry.getValue());
        }
        
        Tuple _tuple = cacheConnection.getTaranConn().findOne(tableInfo.getSpace(), 0, 0, tuple);
        if(_tuple != null) {
            log.info("userid="+_tuple.getInt(0)+", username=" + _tuple.getString(1, CONFIG.DEFAULT_ENCODE)+", sex=" + (_tuple.getBoolean(2) ? "man" : "women") + ", age=" + _tuple.getInt(3));
        }

        TarantoolResultSetMetaData trsmd = new TarantoolResultSetMetaData(tableInfo, this.fields);
        //封装为标准的ResultSet结果集
        ResultSet rs = new TarantoolReslutSet(_tuple, trsmd);

        return rs;
    }
	
	private void setValue(Tuple tuple, String fieldInfo, Object value) {
        String info[] = fieldInfo.split("\\|");
        if(info[1].equals(CONFIG.TYPE_INT)) {
            tuple.setInt(Integer.parseInt(info[0]), (Integer) value);
        }else if(info[1].equals(CONFIG.TYPE_STR)) {
            tuple.setString(Integer.parseInt(info[0]), String.valueOf(value), CONFIG.DEFAULT_ENCODE);
        }
    }
	
	public int executeUpdate(String sql) throws SQLException {
		try{
			CacheSQLParser parser = new CacheSQLParser(connection, sql).parseUpsert();
			parseU(parser, sql);
			return runU();
		}catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	private void parseU(CacheSQLParser parser, String sql) throws Exception {
		table = parser.getTableName();
        if(table == null) throw new Exception("Please specify the table name to be update.");

        tableInfo = parser.queryTableInfo(table);
        if(tableInfo == null) throw new Exception("table '"+table+"' is not exist.");

        bindCount = parser.getBindCount();

        paramValues = parser.getParamValues(tableInfo);
        
        validateParamValues();
	}
	
	/**
     * 校验更新的字段是否带有唯一索引，如果没有唯一索引字段，无法执行更新操作
     * @throws Exception
     */
    private void validateParamValues() throws Exception {
        JSONObject indexes = new JSONObject(tableInfo.getIndexes());
        boolean flag = false;
        for(String field : paramValues.keySet()) {
            if(indexes.has(field) && indexes.getString(field).equals(CONFIG.INDEX_UNIQUE)) {
                flag = true;
                unique_index_column = field;
                break;
            }
        }

        if(!flag) {
            throw new UnSupportException("The fields to be update have no unique index, can't be updated.");
        }
    }
    
    private Integer runU() throws Exception {

        JSONObject fields = new JSONObject(tableInfo.getFields());

        String fieldInfo = fields.getString(unique_index_column);
        Tuple key = new Tuple(1);
        Object value = paramValues.get(unique_index_column);

        setValue(key, fieldInfo, value);

        Tuple tuple = cacheConnection.getTaranConn().findOne(tableInfo.getSpace(), 0, 0, key);
        if(tuple == null) { //构建新的Tuple，执行插入操作
            if(paramValues.size() != fields.length()){
                throw new UnSupportException("can't insert the record because a few fields have not be specified values");
            } else {
                tuple = new Tuple(fields.length());
                Iterator<?> it = fields.keys();
                while(it.hasNext()) {
                    String _fieldName = (String) it.next();
                    String _fieldInfo = fields.getString(_fieldName);
                    setValue(tuple, _fieldInfo, paramValues.get(_fieldName));
                }
                return cacheConnection.getTaranConn().insert(tableInfo.getSpace(), tuple);
            }
        }else {
            for(Map.Entry<String, Object> entry : paramValues.entrySet()) {
                String _fieldInfo = fields.getString(entry.getKey());
                setValue(tuple, _fieldInfo, entry.getValue());
            }
            return cacheConnection.getTaranConn().replace(tableInfo.getSpace(), tuple);
        }

    }
	
	public void close() throws SQLException {
		connection.close();
	}

	public int getMaxFieldSize() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setMaxFieldSize(int max) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public int getMaxRows() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setMaxRows(int max) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public int getQueryTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setQueryTimeout(int seconds) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void cancel() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public SQLWarning getWarnings() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void clearWarnings() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setCursorName(String name) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public boolean execute(String sql) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public ResultSet getResultSet() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getUpdateCount() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean getMoreResults() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setFetchDirection(int direction) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public int getFetchDirection() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setFetchSize(int rows) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public int getFetchSize() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getResultSetConcurrency() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getResultSetType() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addBatch(String sql) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void clearBatch() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public int[] executeBatch() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Connection getConnection() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getMoreResults(int current) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public ResultSet getGeneratedKeys() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public int getResultSetHoldability() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isClosed() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setPoolable(boolean poolable) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public boolean isPoolable() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
