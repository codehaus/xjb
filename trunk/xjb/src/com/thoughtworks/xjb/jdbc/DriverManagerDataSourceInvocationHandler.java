/*
 * Created on 11-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.thoughtworks.xjb.cmt.TransactionGetter;


class DriverManagerDataSourceInvocationHandler implements InvocationHandler {
	private final TransactionGetter transactionGetter;
	private final String url;
	private final String defaultUser;
	private final String defaultPassword;
	
	public DriverManagerDataSourceInvocationHandler(TransactionGetter transactionGetter, String url, String user, String password) {
        this.transactionGetter = transactionGetter;
		this.url = url;
		this.defaultUser = user;
		this.defaultPassword = password;
	}
	
	public DriverManagerDataSourceInvocationHandler(TransactionGetter transactionGetter, String url) {
        this(transactionGetter, url, "", "");
	}
    
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ("getConnection".equals(method.getName())) {
            Connection conn = getConnection(args);
            transactionGetter.getTransaction().registerConnection(conn);
            return conn;
		}
		else if ("toString".equals(method.getName())) {
			return "XJB DriverManager DataSource: " + url + ", " + defaultUser + ", " + defaultPassword;
		}
		else {
			throw new UnsupportedOperationException(method.getName());
		}
	}

	private Connection getConnection(Object[] args) throws SQLException {
		final String user;
		final String password;
		if (args != null && args.length == 2) {
		    user = (String) args[0];
		    password = (String) args[1];
		}
		else {
		    user = defaultUser;
		    password = defaultPassword;
		}
		Connection conn = DriverManager.getConnection(url, user, password);
		return conn;
	}
	
}