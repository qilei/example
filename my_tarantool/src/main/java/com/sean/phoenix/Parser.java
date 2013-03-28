package com.sean.phoenix;

import java.io.StringReader;

import com.salesforce.phoenix.parse.SQLParser;
import com.salesforce.phoenix.parse.SQLStatement;

public class Parser {
	
	public static SQLStatement parse(String sql) throws Exception {
		SQLParser parser = new SQLParser(new StringReader(sql));
		return parser.parseStatement();
	}
}
