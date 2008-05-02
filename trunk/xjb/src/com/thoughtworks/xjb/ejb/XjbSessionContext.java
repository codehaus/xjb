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
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbSessionContext implements SessionContext {
    private final EJBHome home;
    private final EJBObject remote;
    
    private boolean rollbackOnly = false;

    public XjbSessionContext(EJBHome home, EJBObject remote) {
        this.home = home;
        this.remote = remote;
    }
    
	public EJBObject getEJBObject() throws IllegalStateException {
        return remote;
    }

    public EJBHome getEJBHome() {
        return home;
    }

    public void setRollbackOnly() throws IllegalStateException {
        rollbackOnly = true;
    }

    public boolean getRollbackOnly() throws IllegalStateException {
        return rollbackOnly;
    }
    
    // NOTHING IS IMPLEMENTED BELOW HERE!!
    
    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public EJBLocalHome getEJBLocalHome() {
        throw new UnsupportedOperationException();
    }

    public Properties getEnvironment() {
        throw new UnsupportedOperationException();
    }

    public Identity getCallerIdentity() {
        throw new UnsupportedOperationException();
    }

    public Principal getCallerPrincipal() {
        throw new UnsupportedOperationException();
    }

    public boolean isCallerInRole(Identity arg0) {
        throw new UnsupportedOperationException();
    }

    public boolean isCallerInRole(String arg0) {
        throw new UnsupportedOperationException();
    }

    public UserTransaction getUserTransaction() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public TimerService getTimerService() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

	public MessageContext getMessageContext() throws IllegalStateException {
		throw new UnsupportedOperationException();
	}

    public Object getBusinessObject(Class arg0) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public Class getInvokedBusinessInterface() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    public Object lookup(String arg0) {
        throw new UnsupportedOperationException();
    }
}
