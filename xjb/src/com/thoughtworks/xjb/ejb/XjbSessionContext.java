/*
 * Created on 04-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.ejb;

import java.security.Identity;
import java.security.Principal;
import java.util.Properties;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.transaction.UserTransaction;

import com.thoughtworks.xjb.cmt.Transaction;
import com.thoughtworks.xjb.cmt.TransactionalSessionContext;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbSessionContext implements TransactionalSessionContext {
    private final EJBHome home;
    private final EJBObject remote;
    private Transaction transaction = Transaction.NULL;
    
    private boolean rollbackOnly = false;

    public XjbSessionContext(EJBHome home, EJBObject remote) {
        this.home = home;
        this.remote = remote;
    }
    
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

	public EJBObject getEJBObject() throws IllegalStateException {
        return remote;
    }

    public EJBHome getEJBHome() {
        return home;
    }

    public void setRollbackOnly() throws IllegalStateException {
        transaction.setRollbackOnly();
        rollbackOnly = true;
    }

    public boolean getRollbackOnly() throws IllegalStateException {
        return rollbackOnly;
    }
    
    // NOTHING IS IMPLEMENTED BELOW HERE!!
    
    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        throw new UnsupportedOperationException("getEJBLocalObject");
    }

    public EJBLocalHome getEJBLocalHome() {
        throw new UnsupportedOperationException("getEJBLocalHome");
    }

    public Properties getEnvironment() {
        throw new UnsupportedOperationException("getEnvironment");
    }

    public Identity getCallerIdentity() {
        throw new UnsupportedOperationException("getCallerIdentity");
    }

    public Principal getCallerPrincipal() {
        throw new UnsupportedOperationException("getCallerPrincipal");
    }

    public boolean isCallerInRole(Identity arg0) {
        throw new UnsupportedOperationException("isCallerInRole");
    }

    public boolean isCallerInRole(String arg0) {
        throw new UnsupportedOperationException("isCallerInRole");
    }

    public UserTransaction getUserTransaction() throws IllegalStateException {
        throw new UnsupportedOperationException("getUserTransaction");
    }
}
