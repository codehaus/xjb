/*
 * Created on 24-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import java.sql.Connection;
import java.util.Collection;

import com.thoughtworks.proxy.toys.nullobject.Null;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public interface Transaction {
	Transaction NULL = (Transaction) Null.object(Transaction.class);
	
    Policy REQUIRED = new Policy("Required");
	Policy REQUIRES_NEW = new Policy("RequiresNew");
	Policy SUPPORTS = new Policy("Supports");
    Policy NOT_SUPPORTED = new Policy("NotSupported");
	Policy MANDATORY = new Policy("Mandatory");
	Policy NEVER = new Policy("Never");

    /**
     * Commit all registered connections unless {@link #setRollbackOnly()} was
     * called, in which case rollback.
     */
	void commitUnlessRollbackOnly();

    /**
     * Rollback all registered connections
     */
	void rollback();

    /**
     * Register a connection with this transaction
     */
	void registerConnection(Connection connection);

    /**
     * Get all registered transactions
     * 
     * @return read-only collection of connections
     */
	Collection getConnections();

	void setRollbackOnly();
}
