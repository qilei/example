package com.sean.cachedb.core;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.tarantool.core.Tuple;

import com.salesforce.phoenix.parse.DeleteStatement;
import com.salesforce.phoenix.parse.SQLStatement;
import com.salesforce.phoenix.parse.UpsertStatement;

public class CachePreparedStatement extends CacheStatement implements PreparedStatement{
	
	private static Logger log = Logger.getLogger(CachePreparedStatement.class.getName());
	
	private Object[] values;
	private String sql;
	private CacheSQLParser parser;
	
	public CachePreparedStatement(Connection conn, String s) throws Exception {
		super(conn);
		this.sql = s;
		
		parser = new CacheSQLParser(connection, sql).parse();
		parse(parser, sql);
		
		if(bindCount > 0) {
			values = new Object[bindCount];
		}
	}

	public ResultSet executeQuery() throws SQLException {
		try{			
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

            int index = Integer.parseInt(String.valueOf(entry.getValue()));

            setValue(tuple, fieldInfo, values[index]);
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
	
	public int executeUpdate() throws SQLException {
		try{			
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

        setValue(key, fieldInfo, values[Integer.parseInt(String.valueOf(value))]);

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

                    int index = Integer.parseInt(String.valueOf(paramValues.get(_fieldName)));
                    setValue(tuple, _fieldInfo, values[index]);
                }
                return cacheConnection.getTaranConn().insert(tableInfo.getSpace(), tuple);
            }
        }else {
            for(Map.Entry<String, Object> entry : paramValues.entrySet()) {
                String _fieldInfo = fields.getString(entry.getKey());

                int index = Integer.parseInt(String.valueOf(entry.getValue()));
                setValue(tuple, _fieldInfo, values[index]);
            }
            return cacheConnection.getTaranConn().replace(tableInfo.getSpace(), tuple);
        }
    }
	
	private Integer runD() throws Exception {
        JSONObject fields = new JSONObject(tableInfo.getFields());

        Tuple tuple = new Tuple(conditions.size());

        for(Map.Entry<String, Object> entry : conditions.entrySet()) {
            String fieldInfo = fields.getString(entry.getKey());

            int index = Integer.parseInt(String.valueOf(entry.getValue()));

            setValue(tuple, fieldInfo, values[index]);
        }

        return cacheConnection.getTaranConn().delete(tableInfo.getSpace(), tuple);
    }

	public void close() throws SQLException {
		super.close();
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

	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int executeUpdate(String sql) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		values[parameterIndex] = x;
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		values[parameterIndex] = x;
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {
		values[parameterIndex] = x;
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		values[parameterIndex] = x;
	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setDate(int parameterIndex, Date x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setTime(int parameterIndex, Time x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setTimestamp(int parameterIndex, Timestamp x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void clearParameters() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public boolean execute() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void addBatch() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setRef(int parameterIndex, Ref x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setClob(int parameterIndex, Clob x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setArray(int parameterIndex, Array x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDate(int parameterIndex, Date x, Calendar cal)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setURL(int parameterIndex, URL x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public ParameterMetaData getParameterMetaData() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNString(int parameterIndex, String value)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scaleOrLength) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
