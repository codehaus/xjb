/*
 * Created on 19-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.example.legacy.ejb;

import java.rmi.RemoteException;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class GreetingServiceBean implements SessionBean {
    public String greet() {
        return "hello";
    }
    
    // session bean lifecycle methods
    public void ejbCreate() {}
    public void setSessionContext(SessionContext ctx) {}
    public void ejbRemove() throws RemoteException {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
}
