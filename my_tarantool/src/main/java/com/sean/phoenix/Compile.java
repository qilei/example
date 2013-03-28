package com.sean.phoenix;

import java.sql.ResultSet;


public interface Compile {	
	void compile() throws Exception;
	ResultSet execute() throws Exception;
}
