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

import com.salesforce.phoenix.parse.DeleteStatement;
import com.salesforce.phoenix.parse.SQLStatement;
import com.salesforce.phoenix.parse.SelectStatement;
import com.salesforce.phoenix.parse.UpsertStatement;

public class CacheStatement implements Statement{
	
	private static Logger log = Logger.getLogger(CacheStatement.class.getName());
	
	protected Connection connection;
	protected CacheConnection cacheConnection;
	
	protected String table;
	protected Map<String, String> fields;
	protected Map<String, Object> conditions;
	protected Map<String, Object> paramValues;
	protected int bindCount;

	protected MetaData tableInfo;
	protected String unique_index_column;
	
	public CacheStatement(Connection conn) {
		this.connection = conn;
		cacheConnection = (CacheConnection) conn;
	}
	
	public ResultSet executeQuery(String sql) throws SQLException {
		try{
			
			CacheSQLParser parser = new CacheSQLParser(connection, sql).parseQuery();
			parse(parser, sql);
			
			return runQ();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
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
	
	public int executeUpdate(String sql) throws SQLException {
		try{
			CacheSQLParser parser = new CacheSQLParser(connection, sql).parse();
			parse(parser, sql);
			
			SQLStatement statement = parser.getStatement();
			if(statement instanceof UpsertStatement) {
				return runU();
			}else if(statement instanceof DeleteStatement){
				return runD();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
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
    
    private Integer runD() throws Exception {
        JSONObject fields = new JSONObject(tableInfo.getFields());

        Tuple tuple = new Tuple(conditions.size());

        for(Map.Entry<String, Object> entry : conditions.entrySet()) {
            String fieldInfo = fields.getString(entry.getKey());
            setValue(tuple, fieldInfo, entry.getValue());
        }

        return cacheConnection.getTaranConn().delete(tableInfo.getSpace(), tuple);
    }
    
    protected void parse(CacheSQLParser parser, String sql) throws Exception {
		table = parser.getTableName();
        if(table == null) throw new Exception("Please specify the table name to be update.");

        tableInfo = parser.queryTableInfo(table);
        if(tableInfo == null) throw new Exception("table '"+table+"' is not exist.");

        bindCount = parser.getBindCount();
        
        SQLStatement statement = parser.getStatement();
        if(statement instanceof SelectStatement) {
        	fields = parser.getSelectFields(tableInfo);
            if(fields == null) throw new Exception("Please specify the fields to be search.");

            conditions = parser.getWhere(tableInfo);
            if(conditions == null) throw new UnSupportException("Please specify query condition in where clause.");
            
        }else if(statement instanceof UpsertStatement) {
        	paramValues = parser.getParamValues(tableInfo);  
            validateParamValues();
            
        }else if(statement instanceof DeleteStatement) {
        	conditions = parser.getWhere(tableInfo);
        	if(conditions == null) throw new UnSupportException("Please specify query condition in where clause.");
        }
	}
	
	/**
     * 校验更新的字段是否带有唯一索引，如果没有唯一索引字段，无法执行更新操作
     * @throws Exception
     */
    protected void validateParamValues() throws Exception {
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
    
    protected void setValue(Tuple tuple, String fieldInfo, Object value) {
        String info[] = fieldInfo.split("\\|");
        if(info[1].equals(CONFIG.TYPE_INT)) {
            tuple.setInt(Integer.parseInt(info[0]), (Integer) value);
        }else if(info[1].equals(CONFIG.TYPE_STR)) {
            tuple.setString(Integer.parseInt(info[0]), String.valueOf(value), CONFIG.DEFAULT_ENCODE);
        }else if(info[1].equals(CONFIG.TYPE_BOOL)) {
            tuple.setBoolean(Integer.parseInt(info[0]), Boolean.valueOf(String.valueOf(value)));
        }
    }
	
	public void close() throws SQLException {
		connection.close();
	}

	public int getMaxFieldSize() throws SQLException {
		return 0;
	}

	public void setMaxFieldSize(int max) throws SQLException {
	}

	public int getMaxRows() throws SQLException {
		return 0;
	}

	public void setMaxRows(int max) throws SQLException {
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
	}

	public int getQueryTimeout() throws SQLException {
		return 0;
	}

	public void setQueryTimeout(int seconds) throws SQLException {
	}

	public void cancel() throws SQLException {
	}

	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	public void clearWarnings() throws SQLException {
	}

	public void setCursorName(String name) throws SQLException {
	}

	public boolean execute(String sql) throws SQLException {
		return false;
	}

	public ResultSet getResultSet() throws SQLException {
		return null;
	}

	public int getUpdateCount() throws SQLException {
		return 0;
	}

	public boolean getMoreResults() throws SQLException {
		return false;
	}

	public void setFetchDirection(int direction) throws SQLException {
	}

	public int getFetchDirection() throws SQLException {
		return 0;
	}

	public void setFetchSize(int rows) throws SQLException {
	}

	public int getFetchSize() throws SQLException {
		return 0;
	}

	public int getResultSetConcurrency() throws SQLException {
		return 0;
	}

	public int getResultSetType() throws SQLException {
		return 0;
	}

	public void addBatch(String sql) throws SQLException {
	}

	public void clearBatch() throws SQLException {
	}

	public int[] executeBatch() throws SQLException {
		return null;
	}

	public Connection getConnection() throws SQLException {
		return null;
	}

	public boolean getMoreResults(int current) throws SQLException {
		return false;
	}

	public ResultSet getGeneratedKeys() throws SQLException {
		return null;
	}

	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		return 0;
	}

	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		return 0;
	}

	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		return 0;
	}

	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		return false;
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		return false;
	}

	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		return false;
	}

	public int getResultSetHoldability() throws SQLException {
		return 0;
	}

	public boolean isClosed() throws SQLException {
		return false;
	}

	public void setPoolable(boolean poolable) throws SQLException {
	}

	public boolean isPoolable() throws SQLException {
		return false;
	}
	
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

}
