/*
 * Created on 04-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.jdbc;

import java.lang.reflect.Proxy;
import java.sql.Connection;

import javax.sql.DataSource;

import com.thoughtworks.xjb.cmt.TransactionAccessor;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
 public class JdbcDataSourceFactory implements DataSourceFactory {
    private final TransactionAccessor transactionAccessor;
    
    public JdbcDataSourceFactory(TransactionAccessor transactionAccessor) {
        this.transactionAccessor = transactionAccessor;
    }

	public JdbcDataSourceFactory() {
		this(TransactionAccessor.NULL);
	}

	public DataSource createNonClosingDataSource(Connection conn) {
        return (DataSource)Proxy.newProxyInstance(DataSource.class.getClassLoader(),
            new Class[] {DataSource.class},
            new SingleConnectionInvocationHandler(conn));
    }

	public DataSource createDriverManagerDataSource(String url, String user, String password) {
        return (DataSource)Proxy.newProxyInstance(DataSource.class.getClassLoader(),
                new Class[] {DataSource.class},
                new DriverManagerDataSourceInvocationHandler(transactionAccessor, url, user, password));
	}

	public DataSource createDriverManagerDataSource(String url) {
        return createDriverManagerDataSource(url, null, null);
	}
}
