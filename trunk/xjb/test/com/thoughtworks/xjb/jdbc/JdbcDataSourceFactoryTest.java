/*
 * Created on 04-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Properties;

import javax.sql.DataSource;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import com.thoughtworks.proxy.toys.nullobject.Null;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class JdbcDataSourceFactoryTest extends MockObjectTestCase {
    
    public void testShouldCreateDataSourceThatReusesSingleConnection() throws Exception {
        // setup
        Connection connection = (Connection) Null.object(Connection.class);
		JdbcDataSourceFactory factory = new JdbcDataSourceFactory();

        // execute
		DataSource ds = factory.createNonClosingDataSource(connection);

        // verify
        assertTrue(Null.isNullObject(ds.getConnection().createStatement()));
    }
    
    public void testShouldIgnoreCloseOnSingleConnectionDataSource() throws Exception {
        // setup
        Mock mock = new Mock(Connection.class);
        mock.expects(never()).method("close").withNoArguments();
        Connection conn = (Connection)mock.proxy();
        
        // execute
        DataSource ds = new JdbcDataSourceFactory().createNonClosingDataSource(conn);
        ds.getConnection().close();
        
        // verify
        mock.verify();
    }
    
    public void testShouldCreateDriverManagerDataSourceWithUserAndPassword() throws Exception {
    	// setup
    	Class.forName("org.hsqldb.jdbcDriver");
        JdbcDataSourceFactory factory = new JdbcDataSourceFactory();
    	
    	// execute
		DataSource ds = factory.createDriverManagerDataSource("jdbc:hsqldb:.", "sa", "");
    	Connection conn1 = ds.getConnection();
    	Connection conn2 = ds.getConnection();
    	
    	// verify
    	assertNotNull(conn1);
    	assertNotNull(conn2);
    	assertTrue(conn1 != conn2);
	}
    
    public void testShouldCreateDriverManagerDataSourceWithoutUserAndPassword() throws Exception {
    	// setup
        Mock driverMock = new Mock(Driver.class);
        driverMock.stubs();
        DriverManager.registerDriver((Driver) driverMock.proxy());
        JdbcDataSourceFactory factory = new JdbcDataSourceFactory();
        Properties expectedProperties = new Properties();
        expectedProperties.setProperty("user", "sa");
        expectedProperties.setProperty("password", "");
        
        // expect
        driverMock.expects(once()).method("connect")
            .with(eq("jdbc:mock"), eq(expectedProperties))
            .will(returnValue(Null.object(Connection.class)));
    	
    	// execute
		DataSource ds = factory.createDriverManagerDataSource("jdbc:mock");
    	Connection conn = ds.getConnection("sa", "");
    	
    	// verify
    	assertNotNull(conn);
        assertTrue(Null.isNullObject(conn));
	}
}
