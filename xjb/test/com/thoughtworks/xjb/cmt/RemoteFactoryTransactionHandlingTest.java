/*
 * Created on 31-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;

import junit.framework.TestCase;

import org.jmock.Mock;
import org.jmock.core.mixin.Invoked;
import org.jmock.core.mixin.Is;
import org.jmock.core.mixin.Return;
import org.jmock.core.mixin.Throw;
import org.jmock.util.Verifier;

import com.thoughtworks.nothing.Null;
import com.thoughtworks.xjb.ejb.SessionBeanSupport;
import com.thoughtworks.xjb.ejb.XjbRemoteFactory;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class RemoteFactoryTransactionHandlingTest extends TestCase {
    private static final String setRollbackOnly = "setRollbackOnly";
	private static final String createTransaction = "createTransaction";
	private static final EJBHome NULL_EJB_HOME = (EJBHome) Null.object(EJBHome.class);

    private static final String afterMethodFails = "afterMethodFails";
	private static final String afterMethodEnds = "afterMethodEnds";
	private static final String beforeMethodStarts = "beforeMethodStarts";
	private static final String doSomething = "doSomething";
	private static final String lookupPolicyFor = "lookupPolicyFor";
	
    public interface Simple extends EJBObject {
		void doSomething() throws RemoteException;
    }
    
    private Mock transactionMock;
    private Mock transactionFactoryMock;
    private Mock policyHandlerMock;
    private Mock policyLookupMock;
    private Mock beanMock;
    
    private Simple remote;
    
    private void verify() {
        Verifier.verifyObject(this);
    }
    
    public void setUp() {
        transactionMock = new Mock(Transaction.class);
        transactionFactoryMock = new Mock(TransactionFactory.class);
        policyHandlerMock = new Mock(TransactionPolicyHandler.class);
        policyHandlerMock.stubs();
        
        policyLookupMock = new Mock(PolicyLookup.class);
        
        beanMock = new Mock(Simple.class);
        remote = (Simple) new XjbRemoteFactory().createRemote(
                "simple", NULL_EJB_HOME, Simple.class, beanMock.proxy(), 
                (PolicyLookup) policyLookupMock.proxy(),
                (TransactionPolicyHandler)policyHandlerMock.proxy());
        }
    
    private Method getMethod(String methodName) throws Exception {
        // TODO Auto-generated method stub
        return Simple.class.getMethod(methodName, null);
    }
    
    public void testShouldCallTransactionPolicyHandlerBeforeMethodStarts() throws Exception {
        // setup
        Policy thePolicy = Transaction.NOT_SUPPORTED;
        
        // expect
        policyLookupMock.expects(Invoked.once())
            .method(lookupPolicyFor).with(Is.equal(getMethod("doSomething")))
            .will(Return.value(thePolicy));
        
        policyHandlerMock.expects(Invoked.once())
            .method(beforeMethodStarts).with(Is.equal(thePolicy));
        
        beanMock.expects(Invoked.once())
            .method(doSomething).withNoArguments()
            .after(policyHandlerMock, beforeMethodStarts);
        
        // execute
        remote.doSomething();
        
        // verify
        verify();
    }

	public void testShouldCallTransactionPolicyHandlerAfterMethodEnds() throws Exception {
        // setup
        Policy thePolicy = Transaction.REQUIRED;
        
        // expect
        policyLookupMock.expects(Invoked.once())
            .method(lookupPolicyFor).with(Is.equal(getMethod("doSomething")))
            .will(Return.value(thePolicy));
        
        beanMock.expects(Invoked.once())
            .method(doSomething).withNoArguments();
        
        policyHandlerMock.expects(Invoked.once())
            .method(afterMethodEnds).withNoArguments()
            .after(beanMock, doSomething);
        
        // execute
        remote.doSomething();
        
        // verify
        verify();
    }
    
    public void testShouldCallTransactionPolicyHandlerAfterMethodFails() throws Exception {
        // setup
        RuntimeException anException = new RuntimeException("oops");
        Policy thePolicy = Transaction.REQUIRED;
        
        // expect
        policyLookupMock.expects(Invoked.once())
            .method(lookupPolicyFor).with(Is.equal(getMethod("doSomething")))
            .will(Return.value(thePolicy));
        
		beanMock.expects(Invoked.once())
            .method(doSomething).withNoArguments()
            .will(Throw.exception(anException));
        
        policyHandlerMock.expects(Invoked.once())
            .method(afterMethodFails).withNoArguments()
            .after(beanMock, doSomething);
        
        // execute
        try {
			remote.doSomething();
			
		} catch (RuntimeException expected) {
		}
        
        // verify
        verify();
    }
    
    public interface DoesRollback extends EJBObject {
        void doRollback() throws RemoteException;
    }
    
    public static class DoesRollbackBean extends SessionBeanSupport {
        private SessionContext context;

        public void setSessionContext(SessionContext context) {
            this.context = context;
        }
        
        public void doRollback() {
            context.setRollbackOnly();
        }
    }
    
    public void testShouldSetRollbackOnlyInTransactionWhenSetRollbackOnlyInvokedOnSessionContext() throws Exception {
        // setup
        transactionMock.stubs();
        TransactionPolicyHandler handler =
            new XjbTransactionHandler((TransactionFactory) transactionFactoryMock.proxy());
        
        // expect
        policyLookupMock.expects(Invoked.once()).method(lookupPolicyFor).withAnyArguments()
		    .will(Return.value(Transaction.REQUIRES_NEW));

        transactionFactoryMock.expects(Invoked.once()).method(createTransaction).withNoArguments()
            .will(Return.value(transactionMock.proxy()));
        
        transactionMock.expects(Invoked.once()).method(setRollbackOnly).withNoArguments();
        
        // execute
        DoesRollback doesRollback = (DoesRollback) new XjbRemoteFactory().createRemote(
                "ejb", NULL_EJB_HOME, DoesRollback.class, new DoesRollbackBean(),
                (PolicyLookup) policyLookupMock.proxy(),
                handler);
        
        doesRollback.doRollback();
        
        // verify
        verify();
    }
}
