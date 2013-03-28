package com.sean.phoenix;

import java.util.Random;

import com.salesforce.phoenix.parse.SQLStatement;
import com.salesforce.phoenix.parse.SelectStatement;
import com.sean.my_tarantool.Connection;

public class TestCompile {
	public static void main(String[] args) throws Exception {
		int userid = new Random().nextInt(100);
		String sql = "select userid,username from users where userid = " + userid;
		
		Connection connection = new Connection();
		
		SQLStatement statement = Parser.parse(sql);
		Compile compiler = new SelectCompile((SelectStatement) statement, connection);
		compiler.compile();
		compiler.execute();
	}
}
