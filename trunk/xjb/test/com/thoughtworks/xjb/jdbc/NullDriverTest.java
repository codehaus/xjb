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

import com.thoughtworks.proxy.toys.nullobject.Null;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class NullDriverTest extends TestCase {
    public void setUp() {
        NullDriver.clear();
    }
    
    public void testShouldRegisterItselfAgainstUrlPrefix() throws Exception {
		// execute
        NullDriver.registerDriverForPrefix("jdbc:foo");
        NullDriver.registerDriverForPrefix("jdbc:bar");
        
		// verify
        assertTrue(DriverManager.getDriver("jdbc:foo:url") instanceof NullDriver);
        assertTrue(DriverManager.getDriver("jdbc:bar:url") instanceof NullDriver);
	}
    
    public void testShouldReturnNullConnection() throws Exception {
		// setup
        NullDriver.registerDriverForPrefix("jdbc:null");
        
		// execute
        Connection conn = DriverManager.getConnection("jdbc:null");
        
		// verify
        assertNotNull(conn);
        assertTrue(Null.isNullObject(conn));
	}
    
    private void assertNotRegistered(String url) {
        try {
//            System.out.println("Checking url: " + url);
//            System.out.println("Found " + DriverManager.getDrivers().);
//            for (Enumeration e = DriverManager.getDrivers(); e != null && e.hasMoreElements(); ) {
//                Object driver = e.nextElement();
//				System.out.println(driver.toString());
//            }
			DriverManager.getConnection(url);
            fail("url " + url + " should not have been registered");
		} catch (SQLException expected) {
		}
    }
    
    public void testShouldNotReturnNullConnectionForUnknownUrl() throws Exception {
		// setup
        NullDriver.class.getName(); // ensure NullDriver is registered

        // execute
        assertNotRegistered("jdbc:unknown");
	}

	public void testShouldClearRegisteredUrls() throws Exception {
		// setup
		NullDriver.registerDriverForPrefix("jdbc:foo");
		NullDriver.registerDriverForPrefix("jdbc:bar");
        
		// execute
        NullDriver.clear();
        
		// verify
        assertNotRegistered("jdbc:foo");
        assertNotRegistered("jdbc:bar");
	}
    
    public void testShouldDeregisterIndividualUrlPrefix() throws Exception {
		// setup
        NullDriver.clear();
        NullDriver.registerDriverForPrefix("jdbc:foo");
        NullDriver.registerDriverForPrefix("jdbc:bar");
		
		// execute
        NullDriver.deregisterDriverForPrefix("jdbc:foo");
        
		// verify
        assertNotRegistered("jdbc:foo");
        assertTrue(DriverManager.getDriver("jdbc:bar") instanceof NullDriver);
	}
}
