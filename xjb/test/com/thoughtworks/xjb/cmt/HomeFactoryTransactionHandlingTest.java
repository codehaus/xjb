/*
 * Created on 02-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import junit.framework.TestCase;

import org.jmock.Mock;
import org.jmock.core.mixin.Invoked;
import org.jmock.core.mixin.Return;

import com.thoughtworks.xjb.ejb.SessionBeanSupport;
import com.thoughtworks.xjb.ejb.XjbHomeFactory;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class HomeFactoryTransactionHandlingTest extends TestCase {

    private static final String beforeMethodStarts = "beforeMethodStarts";
    private static final String lookupPolicyFor = "lookupPolicyFor";
    
    public interface Simple extends EJBObject {
        String getSomething() throws Exception;
    }
        
    public interface SimpleHome extends EJBHome {
        Simple create() throws CreateException, RemoteException;
    }
    
    public static class SimpleBean extends SessionBeanSupport {
        public String getSomething() {
            return "something";
        }
    }
    
    public void testShouldConstructRemoteWithCorrectPolicyLookupAndTransactionPolicyHandler() throws Exception {
        // setup
        Mock lookupMock = new Mock(PolicyLookup.class);
        Mock handlerMock = new Mock(TransactionPolicyHandler.class);
        handlerMock.stubs();
        
        // expect
        lookupMock.expects(Invoked.once()).method(lookupPolicyFor).withAnyArguments()
            .will(Return.value(Policy.NULL));
        
        handlerMock.expects(Invoked.once()).method(beforeMethodStarts).withAnyArguments();
        
        // execute
        SimpleHome home = (SimpleHome) new XjbHomeFactory().createHome(
                "simple",
                SimpleHome.class,
                Simple.class,
                new SimpleBean(),
                true,
                (PolicyLookup)lookupMock.proxy(), (TransactionPolicyHandler)handlerMock.proxy());
        home.create().getSomething();
        
        // verify
        lookupMock.verify();
        handlerMock.verify();
    }
}
