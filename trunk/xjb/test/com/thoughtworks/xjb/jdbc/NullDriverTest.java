/*
 * Created on 06-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import junit.framework.TestCase;

import com.thoughtworks.nothing.Null;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class NullDriverTest extends TestCase {
    public void testShouldRegisterItself() throws Exception {
		// execute
        NullDriver.class.getName();
        
		// verify
        assertTrue(DriverManager.getDriver("jdbc:null") instanceof NullDriver);
	}
    
    public void testShouldReturnNullConnection() throws Exception {
		// setup
        NullDriver.class.getName();
        
		// execute
        Connection conn = DriverManager.getConnection("jdbc:null");
        
		// verify
        assertNotNull(conn);
        assertTrue(Null.isNullObject(conn));
	}
    
    public void testShouldNotReturnNullConnectionForUnknownUrl() throws Exception {
		// setup
        NullDriver.class.getName();

        // execute
        try {
			DriverManager.getConnection("jdbc:unknown");
            fail("should have failed");
		} catch (SQLException expected) {
		}
	}
}
