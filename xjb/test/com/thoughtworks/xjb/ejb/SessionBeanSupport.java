/*
 * Created on 30-Jan-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * Stubs for a <tt>SessionBean</tt> implementation class
 * 
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class SessionBeanSupport implements SessionBean {

    /** Not part of the interface but useful */
    public void ejbCreate() {
    }
    
    public void setSessionContext(SessionContext context) throws EJBException, RemoteException {
    }

    public void ejbRemove() throws EJBException, RemoteException {
    }

    public void ejbActivate() throws EJBException, RemoteException {
    }

    public void ejbPassivate() throws EJBException, RemoteException {
    }
}
