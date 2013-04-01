package com.sean.cachedb.core;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.tarantool.core.Tuple;

/**
 * 做tarantool查询时，返回的标准结果集，
 * 该结果集继承自java.sql.ResultSet标准接口，
 * 以便与开发人员做统一操作
 * @author shaojieyue
 * @date 2013-03-28 16:15:48
 */
public class TarantoolReslutSet implements ResultSet {
	
	private List<Tuple> tuples;
	private int currentIndex;
	private int length;
	private Map<String,Integer> map;
	private TarantoolResultSetMetaData trsmd;
	private static final String DEFAULT_ENCODING="utf-8";
	private static final  String DEFAULT_DATE_PATTERN="yyyy-MM-dd hh:mm:ss";
	
	public TarantoolReslutSet(List<Tuple> tuples,TarantoolResultSetMetaData trsmd) {
		super();
		if(tuples==null){
			tuples = new ArrayList<Tuple>(0);
		}
		this.tuples = tuples;
		this.trsmd=trsmd;
		length=tuples.size();	
		this.currentIndex=-1;
	}
	
	public TarantoolReslutSet(Tuple tuple,TarantoolResultSetMetaData trsmd) {
		super();
		tuples = new ArrayList<Tuple>(1);
		if(tuple!=null){
			tuples.add(tuple);
		}
		this.trsmd=trsmd;
		length=tuples.size();
		this.currentIndex=-1;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void setTuple(List<Tuple> tuples) {
		this.tuples = tuples;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean absolute(int index) throws SQLException {
		if(length-1<index){
			throw new SQLException("index out of resultSet size , the resultSet size is "+length);
		}
		
		if(index<0){
			throw new SQLException("error index "+index);
		}
		currentIndex=index;
		return true;
	}

	public void afterLast() throws SQLException {
		currentIndex=length;
	}

	public void beforeFirst() throws SQLException {
		currentIndex=-1;
	}

	public void cancelRowUpdates() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void clearWarnings() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void close() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void deleteRow() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public int findColumn(String columnName) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean first() throws SQLException {
		currentIndex=0;
		return true;
	}

	public Array getArray(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Array getArray(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public InputStream getAsciiStream(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public InputStream getAsciiStream(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public BigDecimal getBigDecimal(int index) throws SQLException {
		return tuples.get(currentIndex).getBigDecimal(trsmd.getTarantToolColumnIndex(index));
	}

	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		return tuples.get(currentIndex).getBigDecimal(trsmd.getTarantToolColumnIndex(columnName));
	}

	public BigDecimal getBigDecimal(int arg0, int arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public BigDecimal getBigDecimal(String arg0, int arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public InputStream getBinaryStream(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public InputStream getBinaryStream(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Blob getBlob(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Blob getBlob(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean getBoolean(int index) throws SQLException {
		return tuples.get(currentIndex).getBoolean(trsmd.getTarantToolColumnIndex(index));
	}

	public boolean getBoolean(String columnName) throws SQLException {
		return tuples.get(currentIndex).getBoolean(trsmd.getTarantToolColumnIndex(columnName));
	}

	public byte getByte(int index) throws SQLException {
		Integer val= tuples.get(currentIndex).getInt(trsmd.getTarantToolColumnIndex(index));
		return val.byteValue();
	}

	public byte getByte(String columnName) throws SQLException {
		Integer val= tuples.get(currentIndex).getInt(trsmd.getTarantToolColumnIndex(columnName));
		return val.byteValue();
	}

	public byte[] getBytes(int index) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public byte[] getBytes(String columnName) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Reader getCharacterStream(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Reader getCharacterStream(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Clob getClob(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Clob getClob(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public int getConcurrency() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public String getCursorName() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Date getDate(int index) throws SQLException {
		return getDate(index,DEFAULT_DATE_PATTERN);
	}
	
	/**
	 * 获取日期
	 * @param index 索引
	 * @param pattern 日期格式，默认yyyy-MM-dd hh:mm:ss
	 * @return 转换后的日期
	 * @throws SQLException
	 */
	public Date getDate(int index,String pattern)throws SQLException {
		String val= tuples.get(currentIndex).getString(trsmd.getTarantToolColumnIndex(index),DEFAULT_ENCODING );
		
		if(pattern==null){//日期格式判断
			throw new IllegalArgumentException("pattern must not be null");
		}
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		java.util.Date date=null;
		try {
			date = sdf.parse(val);
		} catch (ParseException e) {
			throw new SQLException("parse Date error, date string is '"+val+"' format date pattern is "+pattern);
		}
		return new java.sql.Date( date.getTime());
	}

	public Date getDate(String columnName) throws SQLException {
		return getDate(columnName,DEFAULT_ENCODING);
	}
	
	public Date getDate(String columnName,String pattern) throws SQLException {
		String val = tuples.get(currentIndex).getString(trsmd.getTarantToolColumnIndex(columnName), DEFAULT_ENCODING);
		if (pattern == null) {// 日期格式判断
			throw new IllegalArgumentException("pattern must not be null");
		}
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		java.util.Date date = null;
		try {
			date = sdf.parse(val);
		} catch (ParseException e) {
			throw new SQLException("parse Date error, date string is '" + val
					+ "' format date pattern is " + pattern);
		}
		return new java.sql.Date(date.getTime());
	}

	public Date getDate(int arg0, Calendar arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Date getDate(String arg0, Calendar arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public double getDouble(int index) throws SQLException {
		return tuples.get(currentIndex).getDouble(trsmd.getTarantToolColumnIndex(index));
	}

	public double getDouble(String columnName) throws SQLException {
		
		return  tuples.get(currentIndex).getDouble(trsmd.getTarantToolColumnIndex(columnName));
	}

	public int getFetchDirection() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public int getFetchSize() throws SQLException {
		return length;
	}

	public float getFloat(int index) throws SQLException {
		return tuples.get(currentIndex).getFloat(trsmd.getTarantToolColumnIndex(index));
	}

	public float getFloat(String columnName) throws SQLException {
		return tuples.get(currentIndex).getFloat(trsmd.getTarantToolColumnIndex(columnName));
	}

	public int getHoldability() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public int getInt(int index) throws SQLException {
		return tuples.get(currentIndex).getInt(trsmd.getTarantToolColumnIndex(index));
	}

	public int getInt(String columnName) throws SQLException {
		return tuples.get(currentIndex).getInt(trsmd.getTarantToolColumnIndex(columnName));
	}

	public long getLong(int index) throws SQLException {
		return tuples.get(currentIndex).getLong(trsmd.getTarantToolColumnIndex(index));
	}

	public long getLong(String columnName) throws SQLException {
		
		return  tuples.get(currentIndex).getLong(trsmd.getTarantToolColumnIndex(columnName));
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Reader getNCharacterStream(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Reader getNCharacterStream(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public NClob getNClob(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public NClob getNClob(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public String getNString(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public String getNString(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Object getObject(int index) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Object getObject(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Object getObject(int arg0, Map<String, Class<?>> arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Object getObject(String arg0, Map<String, Class<?>> arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Ref getRef(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Ref getRef(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public int getRow() throws SQLException {
		return currentIndex+1;
	}

	public RowId getRowId(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public RowId getRowId(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public SQLXML getSQLXML(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public SQLXML getSQLXML(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public short getShort(int index) throws SQLException {
		return tuples.get(currentIndex).getShort(trsmd.getTarantToolColumnIndex(index));
	}

	public short getShort(String columnName) throws SQLException {
		
		return tuples.get(currentIndex).getShort(trsmd.getTarantToolColumnIndex(columnName));
	}

	public Statement getStatement() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public String getString(int index) throws SQLException {
		return getString(index,DEFAULT_ENCODING );
	}

	public String getString(String columnName) throws SQLException {
		
		return getString(columnName,DEFAULT_ENCODING);
	}
	
	public String getString(int index,String encoding)throws SQLException{
		String val= tuples.get(currentIndex).getString(trsmd.getTarantToolColumnIndex(index),encoding );
		return val;
	}
	
	public String getString(String columnName,String encoding) throws SQLException{
		String val= tuples.get(currentIndex).getString(trsmd.getTarantToolColumnIndex(columnName),encoding );
		return val;
	}

	public Time getTime(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Time getTime(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Time getTime(int arg0, Calendar arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Time getTime(String arg0, Calendar arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Timestamp getTimestamp(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Timestamp getTimestamp(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Timestamp getTimestamp(int arg0, Calendar arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public Timestamp getTimestamp(String arg0, Calendar arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public int getType() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public URL getURL(int index) throws SQLException {
		String val= tuples.get(currentIndex).getString(trsmd.getTarantToolColumnIndex(index),DEFAULT_ENCODING );
		URL url=null;
		try {
			url = new URL(val);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("illegal url "+val);
		}
		return url;
	}

	public URL getURL(String columnName) throws SQLException {
		String val= tuples.get(currentIndex).getString(trsmd.getTarantToolColumnIndex(columnName),DEFAULT_ENCODING );
		URL url=null;
		try {
			url = new URL(val);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("illegal url "+val);
		}
		return url;
	}

	public InputStream getUnicodeStream(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public InputStream getUnicodeStream(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public SQLWarning getWarnings() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void insertRow() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean isAfterLast() throws SQLException {
		return this.length>=currentIndex;
	}

	public boolean isBeforeFirst() throws SQLException {
		return currentIndex<0;
	}

	public boolean isClosed() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean isFirst() throws SQLException {
		return currentIndex==0;
	}

	public boolean isLast() throws SQLException {
		return currentIndex==this.length-1;
	}

	public boolean last() throws SQLException {
		currentIndex=length-1;
		return true;
	}

	public void moveToCurrentRow() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void moveToInsertRow() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean next() throws SQLException {
		if(currentIndex+1>length-1){
			return false;
		}
		currentIndex++;//当前索引+1
		return true;
	}

	public boolean previous() throws SQLException {
		if(currentIndex<=0){
			return false;
		}
		currentIndex--;
		return true;
	}

	public void refreshRow() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean relative(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean rowDeleted() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean rowInserted() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public boolean rowUpdated() throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void setFetchDirection(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void setFetchSize(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateArray(int arg0, Array arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateArray(String arg0, Array arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateAsciiStream(int arg0, InputStream arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateAsciiStream(String arg0, InputStream arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateAsciiStream(int arg0, InputStream arg1, int arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateAsciiStream(String arg0, InputStream arg1, int arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateAsciiStream(int arg0, InputStream arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateAsciiStream(String arg0, InputStream arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBigDecimal(String arg0, BigDecimal arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBinaryStream(int arg0, InputStream arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBinaryStream(String arg0, InputStream arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBinaryStream(int arg0, InputStream arg1, int arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBinaryStream(String arg0, InputStream arg1, int arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBinaryStream(int arg0, InputStream arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBinaryStream(String arg0, InputStream arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBlob(int arg0, Blob arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBlob(String arg0, Blob arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBlob(int arg0, InputStream arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBlob(String arg0, InputStream arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBlob(int arg0, InputStream arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBlob(String arg0, InputStream arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBoolean(int arg0, boolean arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBoolean(String arg0, boolean arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateByte(int arg0, byte arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateByte(String arg0, byte arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBytes(int arg0, byte[] arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateBytes(String arg0, byte[] arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateCharacterStream(int arg0, Reader arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateCharacterStream(String arg0, Reader arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateCharacterStream(int arg0, Reader arg1, int arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateCharacterStream(String arg0, Reader arg1, int arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateCharacterStream(int arg0, Reader arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateCharacterStream(String arg0, Reader arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateClob(int arg0, Clob arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateClob(String arg0, Clob arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateClob(int arg0, Reader arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateClob(String arg0, Reader arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateClob(int arg0, Reader arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateClob(String arg0, Reader arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateDate(int arg0, Date arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateDate(String arg0, Date arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateDouble(int arg0, double arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateDouble(String arg0, double arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateFloat(int arg0, float arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateFloat(String arg0, float arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateInt(int arg0, int arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateInt(String arg0, int arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateLong(int arg0, long arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateLong(String arg0, long arg1) throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateNCharacterStream(int arg0, Reader arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateNCharacterStream(String arg0, Reader arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateNCharacterStream(int arg0, Reader arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();
	}

	public void updateNCharacterStream(String arg0, Reader arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateNClob(int arg0, NClob arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateNClob(String arg0, NClob arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateNClob(int arg0, Reader arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateNClob(String arg0, Reader arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateNClob(int arg0, Reader arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateNClob(String arg0, Reader arg1, long arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateNString(int arg0, String arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateNString(String arg0, String arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateNull(int arg0) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateNull(String arg0) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateObject(int arg0, Object arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateObject(String arg0, Object arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateObject(int arg0, Object arg1, int arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateObject(String arg0, Object arg1, int arg2)
			throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateRef(int arg0, Ref arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateRef(String arg0, Ref arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateRow() throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateRowId(int arg0, RowId arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateRowId(String arg0, RowId arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateShort(int arg0, short arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateShort(String arg0, short arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateString(int arg0, String arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateString(String arg0, String arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateTime(int arg0, Time arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateTime(String arg0, Time arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateTimestamp(int arg0, Timestamp arg1) throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public void updateTimestamp(String arg0, Timestamp arg1)
			throws SQLException {
		throw new UnsupportedFeatureException();

	}

	public boolean wasNull() throws SQLException {
		throw new UnsupportedFeatureException();
	}
	
}
