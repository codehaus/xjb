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
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.mixin.Invoked;
import org.jmock.core.mixin.Is;
import org.jmock.core.mixin.Return;
import org.jmock.core.mixin.Throw;

import com.thoughtworks.proxy.toys.nullobject.Null;
import com.thoughtworks.xjb.ejb.RemoteFactory;
import com.thoughtworks.xjb.ejb.SessionBeanSupport;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class TransactionalRemoteFactoryTest extends MockObjectTestCase {
    private static final String setRollbackOnly = "setRollbackOnly";
	private static final String createTransaction = "createTransaction";
	private static final EJBHome NULL_EJB_HOME = (EJBHome) Null.object(EJBHome.class);

    private static final String onFailure = "onFailure";
	private static final String onSuccess = "onSuccess";
	private static final String onInvoke = "onInvoke";
	private static final String doSomething = "doSomething";
	private static final String lookupPolicyFor = "lookupPolicyFor";
	
    public interface Simple extends EJBObject {
		void doSomething() throws RemoteException;
    }
    
    public interface SimpleBean extends Simple, SessionBean {
    }
    
    private Mock transactionMock;
    private Mock transactionFactoryMock;
    private Mock transactionGetterMock;
    private Mock policyHandlerMock;
    private Mock policyLookupMock;
    private Mock beanMock;
    
    private Simple remote;
    
    public void setUp() {
        transactionMock = new Mock(Transaction.class);
        transactionFactoryMock = new Mock(TransactionFactory.class);
        policyHandlerMock = new Mock(TransactionPolicyHandler.class);
        policyHandlerMock.stubs();
        transactionGetterMock = new Mock(TransactionGetter.class);
        policyLookupMock = new Mock(PolicyLookup.class);
        beanMock = new Mock(SimpleBean.class);
        beanMock.stubs();
        
        RemoteFactory factory = new CmtRemoteFactory(
		                (TransactionGetter) transactionGetterMock.proxy(),
		                (PolicyLookup) policyLookupMock.proxy(),
		                (TransactionPolicyHandler)policyHandlerMock.proxy()
		        );
		remote = (Simple) factory.createRemote(
                "simple", NULL_EJB_HOME, Simple.class, beanMock.proxy());
    }
    
    private Method getSimpleMethod(String methodName) throws Exception {
        return Simple.class.getMethod(methodName, null);
    }
    
    public void testShouldCallTransactionPolicyHandlerBeforeMethodStarts() throws Exception {
        // setup
        Policy thePolicy = Transaction.NOT_SUPPORTED;
        
        // expect
        policyLookupMock.expects(Invoked.once())
            .method(lookupPolicyFor).with(Is.equal(getSimpleMethod("doSomething")))
            .will(Return.value(thePolicy));
        
        policyHandlerMock.expects(Invoked.once())
            .method(onInvoke).with(Is.equal(thePolicy));
        
        beanMock.expects(Invoked.once())
            .method(doSomething).withNoArguments()
            .after(policyHandlerMock, onInvoke);
        
        // execute
        remote.doSomething();
    }

	public void testShouldCallTransactionPolicyHandlerAfterMethodEnds() throws Exception {
        // setup
        Policy thePolicy = Transaction.REQUIRED;
        
        // expect
        policyLookupMock.expects(Invoked.once())
            .method(lookupPolicyFor).with(Is.equal(getSimpleMethod("doSomething")))
            .will(Return.value(thePolicy));
        
        beanMock.expects(Invoked.once())
            .method(doSomething).withNoArguments();
        
        policyHandlerMock.expects(Invoked.once())
            .method(onSuccess).withNoArguments()
            .after(beanMock, doSomething);
        
        // execute
        remote.doSomething();
    }
    
    public void testShouldCallTransactionPolicyHandlerAfterMethodFails() throws Exception {
        // setup
        RuntimeException anException = new RuntimeException("oops");
        Policy thePolicy = Transaction.REQUIRED;
        
        // expect
        policyLookupMock.expects(Invoked.once())
            .method(lookupPolicyFor).with(Is.equal(getSimpleMethod("doSomething")))
            .will(Return.value(thePolicy));
        
		beanMock.expects(Invoked.once())
            .method(doSomething).withNoArguments()
            .will(Throw.exception(anException));
        
        policyHandlerMock.expects(Invoked.once())
            .method(onFailure).withNoArguments()
            .after(beanMock, doSomething);
        
        // execute
        try {
			remote.doSomething();
			
		} catch (RuntimeException expected) {
		}
    }
    
    public interface DoesRollback extends EJBObject {
        void setRollbackOnly() throws RemoteException;
    }
    
    public static class DoesRollbackBean extends SessionBeanSupport {
        public SessionContext context;

        public void setSessionContext(SessionContext context) {
            System.out.println("Context = " + context);
            this.context = context;
        }
        
        public void setRollbackOnly() {
            context.setRollbackOnly();
        }
    }
    
    public void testShouldSetRollbackOnlyInTransactionWhenSetRollbackOnlyInvokedOnSessionContext() throws Exception {
        // setup
        transactionMock.stubs();
        TransactionPolicyHandler handler =
            new XjbTransactionHandler((TransactionFactory) transactionFactoryMock.proxy());
        DoesRollbackBean impl = new DoesRollbackBean();
        
        // expect
        policyLookupMock.expects(Invoked.once()).method(lookupPolicyFor).withAnyArguments()
		    .will(Return.value(Transaction.REQUIRES_NEW));

        transactionFactoryMock.expects(Invoked.once()).method(createTransaction).withNoArguments()
            .will(Return.value(transactionMock.proxy()));
        
        transactionGetterMock.expects(Invoked.once()).method("getTransaction").withNoArguments()
            .will(Return.value(transactionMock.proxy()));
        
        transactionMock.expects(Invoked.once()).method(setRollbackOnly).withNoArguments();
        
        // execute
        RemoteFactory factory = new CmtRemoteFactory(
                (TransactionGetter)transactionGetterMock.proxy(),
                (PolicyLookup) policyLookupMock.proxy(),
                handler);
        
		DoesRollback doesRollback = (DoesRollback) factory.createRemote(
                "ejb", NULL_EJB_HOME, DoesRollback.class, impl);
        
        doesRollback.setRollbackOnly();
        assertTrue("rollbackOnly", impl.context.getRollbackOnly());
    }
}