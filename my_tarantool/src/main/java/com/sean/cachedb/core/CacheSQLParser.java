package com.sean.cachedb.core;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.salesforce.phoenix.parse.AliasedParseNode;
import com.salesforce.phoenix.parse.BindParseNode;
import com.salesforce.phoenix.parse.ColumnParseNode;
import com.salesforce.phoenix.parse.DeleteStatement;
import com.salesforce.phoenix.parse.EqualParseNode;
import com.salesforce.phoenix.parse.LiteralParseNode;
import com.salesforce.phoenix.parse.NamedTableNode;
import com.salesforce.phoenix.parse.ParseNode;
import com.salesforce.phoenix.parse.RHSLiteralStatementRewriter;
import com.salesforce.phoenix.parse.SQLParser;
import com.salesforce.phoenix.parse.SQLStatement;
import com.salesforce.phoenix.parse.SelectStatement;
import com.salesforce.phoenix.parse.TableNode;
import com.salesforce.phoenix.parse.UpsertStatement;
import com.salesforce.phoenix.parse.WildcardParseNode;

public class CacheSQLParser {
	
	private static Logger log = Logger.getLogger(CacheSQLParser.class.getName());
	
	private Connection connection;
	private String sql;
	private SQLParser parser;
	private SQLStatement statement;
	
	private SelectStatement selectStatement;
	private UpsertStatement upsertStatement;
	private DeleteStatement deleteStatement;
	
	public CacheSQLParser(Connection connection, String sql) {
		this.connection = connection;
		this.sql = sql;
		try{
			parser = new SQLParser(new StringReader(sql));
			if(parser == null) {
				log.info("create object 'CacheSQLParser' fail.");
			}
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public CacheSQLParser parseQuery() throws Exception {
		selectStatement = parser.parseQuery();
		selectStatement = RHSLiteralStatementRewriter.normalizeWhereClause(selectStatement);
		statement = selectStatement;
		return this;
		
	}
	
	public CacheSQLParser parseUpsert() throws Exception {
		upsertStatement = parser.parseUpsert();
		statement = upsertStatement;
		return this;
	}
	
	public CacheSQLParser parse() throws Exception {
		statement = parser.parseStatement();
		if(statement instanceof SelectStatement) {
			selectStatement = (SelectStatement) statement;
		}else if(statement instanceof UpsertStatement) {
			upsertStatement = (UpsertStatement) statement;
		}else if(statement instanceof DeleteStatement) {
			deleteStatement = (DeleteStatement) statement;
		}
		return this;
	}
	
	public String getTableName() throws Exception {
		
		if(statement instanceof SelectStatement) {
			List<TableNode> fromNodes = selectStatement.getFrom();
			
			if(fromNodes != null) {
				if (fromNodes.size() > 1) {
		            throw new UnSupportException("Joins not supported");
		        }
				
				if(fromNodes != null && fromNodes.size() > 0) {
		            NamedTableNode tableNode = (NamedTableNode) fromNodes.get(0);
		            return tableNode.getName().getTableName().toLowerCase();
		        }
				
			}
			
		}else if(statement instanceof UpsertStatement) {
			return upsertStatement.getTable().getTableName().toLowerCase();
		}else if(statement instanceof DeleteStatement) {
			return deleteStatement.getTable().getTableName().toLowerCase();
		}
		
		
		return null;

	}
	
	public MetaData queryTableInfo(String tablename) {
		CacheConnection c = (CacheConnection) connection;
		List<MetaData> metas = c.getTemplate().find(MetaData.class, 0, "tablename").condition(tablename).list();
		if(metas != null && metas.size() > 0) {
			return metas.get(0);
		}
		return null;
	}
	
	public int getBindCount() {
		return statement.getBindCount();
	}
	
	public Map<String, String> getSelectFields(MetaData tableInfo) throws Exception {
        
		if(selectStatement == null) throw new Exception("This SQL statement is not support for this method.");
        
		List<AliasedParseNode> nodes =  selectStatement.getSelect();
        if(nodes != null) {
            Map<String, String> fields = new HashMap<String, String>();
            if(nodes.get(0).getNode() instanceof WildcardParseNode) {
                JSONObject fieldInfo = new JSONObject(tableInfo.getFields());
                Iterator<?> it = fieldInfo.keys();
                while(it.hasNext()) {
                    String columName = String.valueOf(it.next());
                    int spaceFieldIndex = Integer.valueOf(fieldInfo.getString(columName).split("\\|")[0]) ;
                    fields.put(columName, columName);
                    fields.put((spaceFieldIndex+1)+"", columName); //spaceFieldIndex 和 columnIndex关系是相差1，因为ResultSet 的索引列是从1开始的
                }
            } else {
                int index = 1;
                for(AliasedParseNode node : nodes) {
                    ColumnParseNode columnNode = (ColumnParseNode) node.getNode();
                    String columnName = columnNode.getFullName();
                    String alias = node.getAlias();
                    alias = alias == null ? columnName : alias;
                    fields.put(alias, columnName);
                    fields.put(String.valueOf(index++), columnName);
                }
            }
            return fields;
        }
        return null;
    }
	
	public Map<String, Object> getWhere(MetaData tableInfo) throws Exception {
        Map<String, Object> condition = null;
        ParseNode where = null;
        
        if(statement instanceof SelectStatement) {
        	where = selectStatement.getWhere();
        }else if(statement instanceof DeleteStatement) {
        	where = deleteStatement.getWhere();
        }
        
        if(where != null && where instanceof EqualParseNode) {

            List<ParseNode> nodes = where.getChildren();
            condition = new LinkedHashMap<String, Object>();

            ParseNode valueNode = nodes.get(1);
            Object value = null;
            if(valueNode instanceof LiteralParseNode) {
                value = ((LiteralParseNode) valueNode).getValue();
            }else if(valueNode instanceof BindParseNode) {
                value = ((BindParseNode) valueNode).getIndex();
            }

            ColumnParseNode paramNode = (ColumnParseNode)nodes.get(0);
            if(validateWhere(paramNode, tableInfo)) {
                condition.put(paramNode.getFullName().toLowerCase(), value);
            }else {
                 throw new UnSupportException("The fields in the where clause have no index, can't be searched.");
            }
        }else{
            throw new UnSupportException("has unsupported condition!");
        }
        return condition;
    }

    /**
     * 验证Where条件语句中是否包含带有唯一索引的字段，Tarantoo只能查询带有索引的字段
     * @param paramNode
     * @return
     * @throws JSONException
     */
    private boolean validateWhere(ColumnParseNode paramNode, MetaData tableInfo) throws JSONException {
        JSONObject indexes = new JSONObject(tableInfo.getIndexes());
        boolean  flag = false;
        String index = indexes.getString(paramNode.getFullName().toLowerCase());
        if(index != null && index.equals(CONFIG.INDEX_UNIQUE)) {
            flag = true;
        }
        return flag;
    }
    
    public Map<String, Object> getParamValues(MetaData tableInfo) throws Exception {
    	List<ParseNode> columns = upsertStatement.getColumns();
        List<ParseNode> values = upsertStatement.getValues();
        if(columns.size() > values.size()) {
            throw new UnSupportException("SQL statement has an syntax error! field's number > value's number.");
        }

        Map<String, Object> paramValues = new LinkedHashMap<String, Object>();
        if(columns.size() == 0) {    //如果SQL语句中没有指明更新的字段信息，则按数据表所有字段全部更新，如果给定的参数值数目小于字段数目，则抛出异常

            JSONObject fields = new JSONObject(tableInfo.getFields());

            if(values.size() < fields.length()) {
                throw new IllegalArgumentException("SQL statement has an syntax error! value's number < table's field number.");
            }

            Iterator iterator = fields.keys();
            while(iterator.hasNext()) {
                String column = (String) iterator.next();
                int index = Integer.parseInt(fields.getString(column).split("\\|")[0]);
                Object value = null;
                ParseNode valueNode =  values.get(index);
                if(valueNode instanceof LiteralParseNode) {
                    value = ((LiteralParseNode) valueNode).getValue();
                }else if(valueNode instanceof BindParseNode) {  //如果SQL语句中存在绑定参数
                    value = ((BindParseNode) valueNode).getIndex();
                }
                paramValues.put(column.toLowerCase(), value);
            }
        }else {
            for(int i=0; i<columns.size(); i++) {
                Object value = null;
                ParseNode valueNode =  values.get(i);
                if(valueNode instanceof LiteralParseNode) {
                    value = ((LiteralParseNode) valueNode).getValue();
                }else if(valueNode instanceof BindParseNode) {
                    value = ((BindParseNode) valueNode).getIndex();
                }
                paramValues.put(((ColumnParseNode) columns.get(i)).getFullName().toLowerCase(), value);
            }
        }

        return paramValues;
    }
    
}
