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

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import com.thoughtworks.xjb.ejb.HomeFactory;
import com.thoughtworks.xjb.ejb.SessionBeanSupport;
import com.thoughtworks.xjb.ejb.XjbHomeFactory;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class TransactionalHomeFactoryTest extends MockObjectTestCase {

    private static final String onInvoke = "onInvoke";
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
        lookupMock.expects(once()).method(lookupPolicyFor).withAnyArguments()
            .will(returnValue(Policy.NULL));
        
        handlerMock.expects(once()).method(onInvoke).withAnyArguments();
        
        // execute
        HomeFactory factory = new XjbHomeFactory(
                new CmtRemoteFactory(
                TransactionGetter.NULL,
                (PolicyLookup)lookupMock.proxy(),
                (TransactionPolicyHandler)handlerMock.proxy()));
		SimpleHome home = (SimpleHome) factory.createSessionHome(
                "simple",
                SimpleHome.class,
                Simple.class,
                new SimpleBean(),
                true);
        assertNotNull(home);
        home.create().getSomething();
        
        // verify
        lookupMock.verify();
        handlerMock.verify();
    }
}
