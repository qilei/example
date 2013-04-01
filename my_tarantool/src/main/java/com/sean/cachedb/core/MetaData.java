package com.sean.cachedb.core;

import org.tarantool.facade.annotation.Field;
import org.tarantool.facade.annotation.Index;
import org.tarantool.facade.annotation.Tuple;

/**
 * @author xiaohui
 * 元数据对象，记录数据表的描述信息，包括表名及其对应的space ID，以及表字段信息,索引信息
 */

@Tuple(space = 0)
public class MetaData {
	private String tablename;
	private Integer space;
	private String fields;
    private String indexes;

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

    @Field(value = 3, index = { @Index(fieldNo = 0, indexNo = 3) })
    public String getIndexes() {
        return indexes;
    }

    public void setIndexes(String indexes) {
        this.indexes = indexes;
    }
}
