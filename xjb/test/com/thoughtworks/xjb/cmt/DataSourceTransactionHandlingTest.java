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
import com.thoughtworks.xjb.jdbc.JdbcDataSourceFactory;
import com.thoughtworks.xjb.jdbc.NullDriver;
/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North </a>
 */
public class DataSourceTransactionHandlingTest extends TransactionMockingTestCase {
	public void testShouldCreateDriverManagerDataSourceThatRegistersConnectionsWithCurrentTransaction()
			throws Exception {
		// setup
		final Transaction transaction = new XjbTransaction();
		JdbcDataSourceFactory dataSourceFactory = new JdbcDataSourceFactory(
				new TransactionAccessor() {
					public Transaction getTransaction() {
						return transaction;
					}
				});
		NullDriver.class.getName(); // cause NullDriver to register itself
		DataSource ds = dataSourceFactory
				.createDriverManagerDataSource("jdbc:null");
		// execute
		Connection conn1 = ds.getConnection();
		Connection conn2 = ds.getConnection();
		// verify
		assertTrue(transaction.getConnections().contains(conn1));
		assertTrue(transaction.getConnections().contains(conn2));
	}

    // TODO non-closing connection
//	public void testShouldCreateNonClosingDataSourceThatRegistersConnectionWithCurrentTransaction()
//			throws Exception {
//		fail("TODO");
//	}
}
