/*
 * Created on 23-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.example.legacy.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class ExampleBean implements SessionBean {
    
    public String getSomeValue() throws NamingException {
        Context context = new InitialContext();
        String result = (String)PortableRemoteObject.narrow(context.lookup("SOME_VALUE"), String.class);
        return result;
    }
    
    // ---------------------- EJB lifecycle methods --------------------------
    
    public void ejbCreate() {
        System.out.println("ejbCreate() called");
    }
    
    /**
     * Set the session context - called after instance creation
     */
    public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
        System.out.println("setSessionContext() called");
    }

    /**
     * called to tidy up bean
     */
    public void ejbRemove() throws EJBException, RemoteException {
        System.out.println("ejbRemove() called");
    }

    public void ejbActivate() throws EJBException, RemoteException {
    }

    public void ejbPassivate() throws EJBException, RemoteException {
    }
}
