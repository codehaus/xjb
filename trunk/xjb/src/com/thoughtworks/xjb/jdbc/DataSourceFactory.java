/*
 * Created on 21-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.jdbc;
import java.sql.Connection;

import javax.sql.DataSource;

import com.thoughtworks.nothing.Null;
/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public interface DataSourceFactory {
    DataSourceFactory NULL = (DataSourceFactory) Null.object(DataSourceFactory.class);
    
	DataSource createNonClosingDataSource(Connection conn);
	DataSource createDriverManagerDataSource(String url, String user, String password);
}