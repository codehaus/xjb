/*
 * Created on 06-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import com.thoughtworks.nothing.Null;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class NullDriver implements Driver {

    static {
        try {
            DriverManager.registerDriver(new NullDriver());
        } catch (SQLException e) {
            throw new RuntimeException("Unable to register NullDriver: " + e.getMessage());
        }
    }
    
    // Driver methods
    
	public Connection connect(String url, Properties info) throws SQLException {
		return (acceptsURL(url)) ?
                (Connection) Null.object(Connection.class) : null;
	}
    
	public boolean acceptsURL(String url) throws SQLException {
		return url.startsWith("jdbc:null");
	}
    
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
		return new DriverPropertyInfo[0];
	}
    
	public int getMajorVersion() {
		return 0;
	}
    
	public int getMinorVersion() {
		return 0;
	}
    
	public boolean jdbcCompliant() {
		return false;
	}
}
