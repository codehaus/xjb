/*
 * Created on 01-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import org.jmock.Mock;
import org.jmock.core.mixin.Invoked;
import org.jmock.util.Verifier;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbTransactionTest extends TestCase {
    private static final String commit = "commit";
	private static final String rollback = "rollback";

    private Mock conn1;
    private Mock conn2;
    private Transaction txn;
    
    public void setUp() {
        txn = new XjbTransaction();
        conn1 = new Mock(Connection.class);
        conn2 = new Mock(Connection.class);
        txn.registerConnection((Connection) conn1.proxy());
        txn.registerConnection((Connection) conn2.proxy());
    }
    
    private void verify() throws Exception {
        Verifier.verifyObject(this);
    }
    
    public void testShouldAddTransactions() throws Exception {
		// verify
        Collection conns = txn.getConnections();
        assertEquals(2, conns.size());
        assertTrue(conns.contains(conn1.proxy()));
        assertTrue(conns.contains(conn2.proxy()));
	}
    
    public void testShouldReturnImmutableCollectionOfConnections() throws Exception {
        Collection connections = txn.getConnections();
        try {
            connections.add(new Object());
            fail("add");
        } catch (UnsupportedOperationException expected) {
        }
        try {
            connections.addAll(Collections.singleton(new Object()));
            fail("addAll");
        } catch (UnsupportedOperationException expected) {
        }
        try {
            connections.remove(new Object());
            fail("remove");
        } catch (UnsupportedOperationException expected) {
        }
        try {
            connections.removeAll(Collections.singleton(new Object()));
            fail("removeAll");
        } catch (UnsupportedOperationException expected) {
        }
        try {
            connections.clear();
            fail("clear");
        } catch (UnsupportedOperationException expected) {
        }
	}
    
    public void testShouldCommitConnections() throws Exception {
        // expect
        conn1.expects(Invoked.once()).method(commit).withNoArguments();
        conn2.expects(Invoked.once()).method(commit).withNoArguments();
        
        // execute
        txn.commitUnlessRollbackOnly();
        
        // verify
        verify();
	}
    
    public void testShouldRollbackConnections() throws Exception {
        // expect
        conn1.expects(Invoked.once()).method(rollback).withNoArguments();
        conn2.expects(Invoked.once()).method(rollback).withNoArguments();
        
        // execute
        txn.rollback();
        
        // verify
        verify();
	}
    
    public void testShouldRollbackConnectionsOnCommitIfSetRollbackOnly() throws Exception {
		// setup
        txn.setRollbackOnly();
        
        // expect
        conn1.expects(Invoked.once()).method(rollback).withNoArguments();
        conn2.expects(Invoked.once()).method(rollback).withNoArguments();
        
        // execute
        txn.commitUnlessRollbackOnly();
        
        // verify
        verify();
	}
    
    public void testShouldForgetConnectionsAfterCommit() throws Exception {
        // setup
        conn1.stubs();
        conn2.stubs();
        
		// execute
        txn.commitUnlessRollbackOnly();
        
        // verify
        assertTrue(txn.getConnections().isEmpty());
	}
    
    public void testShouldForgetConnectionsAfterRollback() throws Exception {
        // setup
        conn1.stubs();
        conn2.stubs();
        
		// execute
        txn.rollback();
        
        // verify
        assertTrue(txn.getConnections().isEmpty());
	}
}
