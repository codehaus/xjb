/*
 * Created on 04-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;
import java.sql.Connection;

import javax.sql.DataSource;

import com.thoughtworks.proxy.toys.nullobject.Null;
import com.thoughtworks.xjb.jdbc.DataSourceFactory;
import com.thoughtworks.xjb.jdbc.JdbcDataSourceFactory;
import com.thoughtworks.xjb.jdbc.NullDriver;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North </a>
 */
public class DataSourceTransactionHandlingTest extends TransactionMockingTestCase {
    private Transaction transaction;
    private DataSourceFactory dataSourceFactory;
    
	public void setUp() {
		transaction = new XjbTransaction();
		dataSourceFactory = new JdbcDataSourceFactory(
				new TransactionGetter() {
					public Transaction getTransaction() {
						return transaction;
					}
				});
        NullDriver.registerDriverForPrefix("jdbc:null");
	}
    
    public void tearDown() {
        NullDriver.deregisterDriverForPrefix("jdbc:null");
    }
    
	public void testShouldCreateDriverManagerDataSourceThatRegistersConnectionsWithCurrentTransaction()
			throws Exception {
		DataSource ds = dataSourceFactory
				.createDriverManagerDataSource("jdbc:null");
		
        // execute
		Connection conn1 = ds.getConnection();
		Connection conn2 = ds.getConnection();
		
        // verify
		assertTrue(transaction.getConnections().contains(conn1));
		assertTrue(transaction.getConnections().contains(conn2));
	}

	public void testShouldCreateNonClosingDataSourceThatRegistersConnectionWithCurrentTransaction()
			throws Exception {
        // setup
        Transaction txn1 = transaction;
        Connection conn = (Connection)Null.object(Connection.class);
        DataSource ds = dataSourceFactory.createNonClosingDataSource(conn);
        
        // execute
        Connection conn1 = ds.getConnection();
        Transaction txn2 =
            transaction = new XjbTransaction();
        Connection conn2 = ds.getConnection();
        
        // verify
        assertTrue("txn1 contains conn1", txn1.getConnections().iterator().next() == conn1);
        assertTrue("txn2 contains conn2", txn2.getConnections().iterator().next() == conn2);
	}
}
