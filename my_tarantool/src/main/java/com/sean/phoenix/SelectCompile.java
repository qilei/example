package com.sean.phoenix;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.tarantool.core.Tuple;

import com.salesforce.phoenix.parse.AliasedParseNode;
import com.salesforce.phoenix.parse.ColumnParseNode;
import com.salesforce.phoenix.parse.EqualParseNode;
import com.salesforce.phoenix.parse.LiteralParseNode;
import com.salesforce.phoenix.parse.NamedTableNode;
import com.salesforce.phoenix.parse.ParseNode;
import com.salesforce.phoenix.parse.RHSLiteralStatementRewriter;
import com.salesforce.phoenix.parse.SelectStatement;
import com.salesforce.phoenix.parse.TableNode;
import com.sean.my_tarantool.CONFIG;
import com.sean.my_tarantool.Connection;
import com.sean.my_tarantool.MetaData;


public class SelectCompile implements Compile {
	
	private static Logger log = Logger.getLogger(SelectCompile.class.getName());
	
	private SelectStatement statement;
	private Connection connection;
	
	private String[] fields;
	private String table;
	private Map<String, Object> conditions;
	
	public SelectCompile(SelectStatement statement, Connection connection) {
		this.statement = statement;
		this.connection = connection;
	}

	public void compile() throws Exception {
		statement = RHSLiteralStatementRewriter.normalizeWhereClause(statement);
		fields = getSelectFields();
		table = getTableName();
		conditions = getWhere();
	}

	private String[] getSelectFields() {
		List<AliasedParseNode> nodes =  statement.getSelect();
		if(nodes != null) {
			int size = nodes.size();
			String[] fields = new String[size];
			int index = 0;
			for(AliasedParseNode node : nodes) {
				ColumnParseNode columnNode = (ColumnParseNode) node.getNode();
				fields[index++] = columnNode.getFullName().toLowerCase();
			}
			return fields;
		}
		return null;
	}
	
	private String getTableName() throws Exception {
		List<TableNode> fromNodes = statement.getFrom();
		if (fromNodes.size() > 1) {
            throw new UnSupportException("Joins not supported");
        }
		
		if(fromNodes != null && fromNodes.size() > 0) {
			NamedTableNode tableNode = (NamedTableNode) fromNodes.get(0);
			return tableNode.getName().getTableName().toLowerCase();
		}
		
		return null;
	}
	
	private Map<String, Object> getWhere() throws Exception {
		Map<String, Object> condition = null;
		ParseNode where = statement.getWhere();
		if(where instanceof EqualParseNode) {
			List<ParseNode> nodes = where.getChildren();
			condition = new LinkedHashMap<String, Object>();
			condition.put(((ColumnParseNode)nodes.get(0)).getFullName().toLowerCase(), ((LiteralParseNode) nodes.get(1)).getValue());
		}else{
			throw new UnSupportException("has unsupported condition!");
		}
		return condition;
	}

	public ResultSet execute() throws Exception {
		MetaData tableInfo = connection.queryTableInfo(table);
		
		JSONObject fields = new JSONObject(tableInfo.getFields());
		
		Tuple tuple = new Tuple(1);
		
		int i = 0;
		for(Map.Entry<String, Object> entry : conditions.entrySet()) {
			if(i < 1) {
				String fieldInfo = fields.getString(entry.getKey());
				String type = fieldInfo.split("\\|")[1];
				setValue(tuple, type, entry.getValue());
				i++;
			}else{
				break;
			}
		}
		
		Tuple user = connection.queryOne(tableInfo.getSpace(), 0, 0, tuple);
		log.info("userid="+user.getInt(0)+", username=" + user.getString(1, CONFIG.DEFAULT_ENCODE)+", sex=" + (user.getBoolean(2) ? "man" : "women") + ", age=" + user.getInt(3));
		
		return null;
	}
	
	private void setValue(Tuple tuple, String type, Object value) {
		if(type.equals(CONFIG.TYPE_INT)) {
			tuple.setInt(0, (Integer) value);
		}
	}
}
