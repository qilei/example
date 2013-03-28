package com.sean.my_tarantool;

import org.tarantool.facade.annotation.Field;
import org.tarantool.facade.annotation.Index;
import org.tarantool.facade.annotation.Tuple;

@Tuple(space = 0)
public class MetaData {
	private String tablename;
	private Integer space;
	private String fields;

	@Field(value = 0, index = { @Index(fieldNo = 0, indexNo = 0) })
	public String getTablename() {
		return tablename;
	}
	public void setTablename(String tablename) {
		this.tablename = tablename;
	}
	
	@Field(value = 1, index = { @Index(fieldNo = 0, indexNo = 1) })
	public Integer getSpace() {
		return space;
	}
	public void setSpace(Integer space) {
		this.space = space;
	}
	
	@Field(value = 2, index = { @Index(fieldNo = 0, indexNo = 2) })
	public String getFields() {
		return fields;
	}
	public void setFields(String fields) {
		this.fields = fields;
	}
	
}
