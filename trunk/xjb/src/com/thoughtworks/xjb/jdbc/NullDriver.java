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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.thoughtworks.proxy.toys.nullobject.Null;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class NullDriver implements Driver {
    private static final Driver instance;
    private static final Set urlPrefixes = new HashSet();
    static {
        try {
            instance = new NullDriver();
			DriverManager.registerDriver(instance);
		} catch (SQLException e) {
			throw new Error("Unable to register NullDriver: " + e.getMessage());
		}
    }
    
    public static void registerDriverForPrefix(final String urlPrefix) {
        urlPrefixes.add(urlPrefix);
    }

    public static void deregisterDriverForPrefix(String urlPrefix) {
        urlPrefixes.remove(urlPrefix);
    }

    public static void clear() {
        urlPrefixes.clear();
    }

    // Driver methods

	public Connection connect(String url, Properties info) throws SQLException {
		return (acceptsURL(url)) ?
                (Connection) Null.object(Connection.class) : null;
	}
    
	public boolean acceptsURL(String url) throws SQLException {
        for (Iterator i = urlPrefixes.iterator(); i.hasNext();) {
    		String urlPrefix = (String) i.next();
    		if (url.startsWith(urlPrefix)) {
                return true;
            }
    	}
        return false;
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
