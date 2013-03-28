package com.sean.phoenix;

import java.io.StringReader;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.logging.Logger;

import com.salesforce.phoenix.parse.LimitNode;
import com.salesforce.phoenix.parse.OrderByNode;
import com.salesforce.phoenix.parse.ParseNode;
import com.salesforce.phoenix.parse.RHSLiteralStatementRewriter;
import com.salesforce.phoenix.parse.SQLParser;
import com.salesforce.phoenix.parse.SQLStatement;
import com.salesforce.phoenix.parse.SelectStatement;
import com.salesforce.phoenix.parse.TableNode;

public class TestParse {
	
	private static Logger log = Logger.getLogger(TestParse.class.getName());
	
	public static void main(String[] args) throws Exception {
//		SQLParser parser = new SQLParser(new StringReader(
//	            "select a from b\n" +
//	            "where ((ind.name = 'X')" +
//	            "and rownum <= (1000 + 1000))\n"
//	            ));

		SQLParser parser = new SQLParser(new StringReader(
	            "select /*gatherSlowStats*/ count(1) from core.search_name_lookup ind\n" +
	            "where( (ind.name = 'X'\n" +
	            "and rownum <= 1 + 2)\n" +
	            "and (ind.organization_id = '000000000000000')\n" +
	            "and (ind.key_prefix = '00T')\n" +
	            "and (ind.name_type = 't'))"
	            ));
		
		
		SQLStatement statement = parser.parseStatement();
		
		log.info("parse over!");
		
		String sql = "select userid, username, count(1) from user where userid = 1 and username = 'sean' group by userid,username order by username";
		parseSelect(sql);
	}
	
	public static void parseSelect(String sql) throws Exception {
		SQLParser parser = new SQLParser(new StringReader(sql));
		SQLStatement statement = parser.parseStatement();
		
		SelectStatement _statement = (SelectStatement)statement;
		
		_statement = RHSLiteralStatementRewriter.normalizeWhereClause(_statement);
		
		List<TableNode> fromNodes = _statement.getFrom();
        if (fromNodes.size() > 1) {
            throw new SQLFeatureNotSupportedException("Joins not supported");
        }
		
        LimitNode limitNode = _statement.getLimit();
        
        List<ParseNode> groupByNodes = _statement.getGroupBy();

        int groupByNodeCount = groupByNodes.size();
        
        ParseNode having = _statement.getHaving();
        
        ParseNode where = _statement.getWhere();
        
        List<OrderByNode> orderNodes = _statement.getOrderBy();
        
        log.info("");
	}
}
