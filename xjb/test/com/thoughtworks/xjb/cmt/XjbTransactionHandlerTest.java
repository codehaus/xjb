/*
 * Created on 23-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import javax.ejb.EJBException;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbTransactionHandlerTest extends TransactionMockingTestCase {
    
	public void testShouldStartWithNoTransaction() throws Exception {
		// execute
        TransactionGetter transactionGetter = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // verify
		assertEquals(Transaction.NULL, transactionGetter.getTransaction());
	}
    
    public void testShouldFindExistingTransaction() throws Exception {
        // setup
        installMockTransactionAsCurrentTransaction();
        
        // execute
        Transaction result = currentTransaction();
        
        // verify
        verify();
        assertSame(transactionMock.proxy(), result);
	}
    
    // "Required" transaction policy
    
    public void testShouldCreateTransactionIfNoneExistsWhenRequiredMethodIsInvoked() throws Exception {
        // setup
        factoryMockWillReturnTransactionMock();
        XjbTransactionHandler handler = new XjbTransactionHandler(factory);
        
        // execute
        handler.onInvoke(Transaction.REQUIRED);
        Transaction result = handler.getTransaction();
        
        // verify
        verify();
		assertSame(transactionMock.proxy(), result);
    }
    
    public void testShouldCommitTransactionWhenRequiredMethodSucceeds() throws Exception {
        // setup
        factoryMockWillReturnTransactionMock();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        handler.onInvoke(Transaction.REQUIRED);
        
        // expect
        transactionMock.expects(once()).method(commitUnlessRollbackOnly).withNoArguments();
        
        // execute
        handler.onSuccess();
        
        // verify
        verify();
        TransactionGetter transactionGetter = new XjbTransactionHandler(TransactionFactory.NULL);
		assertEquals(Transaction.NULL, transactionGetter.getTransaction());
    }
    
    public void testShouldReuseExistingTransactionIfOneExistsWhenRequiredMethodIsInvoked() throws Exception {
        // setup
        factoryMockWillReturnTransactionMock();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        handler.onInvoke(Transaction.REQUIRED);
        
        // expect
        otherFactoryMock.expects(never()).method(createTransaction).withNoArguments();
        
        // execute
        TransactionPolicyHandler anotherHandler = new XjbTransactionHandler(otherFactory);
        anotherHandler.onInvoke(Transaction.REQUIRED);
        
        // verify
        verify();
	}
    
    public void testShouldNotCommitTransactionWhenRequiredMethodSucceedsIfThisHandlerDidNotCreateIt() throws Exception {
        // setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler otherHandler = new XjbTransactionHandler(otherFactory);
        
        // expect
        otherTransactionMock.expects(never()).method(commitUnlessRollbackOnly).withNoArguments();
        otherFactoryMock.expects(never()).method(createTransaction).withNoArguments();
        
        // execute
        otherHandler.onInvoke(Transaction.REQUIRED);
        otherHandler.onSuccess();
        
        // verify
        verify();
        assertTrue("should not be NULL", Transaction.NULL != currentTransaction());
	}
    
    public void testShouldRollbackTransactionWhenRequiredMethodFails() throws Exception {
		// setup
        factoryMockWillReturnTransactionMock();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        handler.onInvoke(Transaction.REQUIRED);
        
        // expect
        transactionMock.expects(once()).method(rollback).withNoArguments();
        
        // execute
        handler.onFailure();
        
        // verify
        verify();
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldNotRollbackTransactionWhenRequiredMethodFailsIfThisHandlerDidNotCreateIt() throws Exception {
        // setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        handler.onInvoke(Transaction.REQUIRED);
        
        // expect
        transactionMock.expects(never()).method(rollback).withNoArguments();
        
        // execute
        handler.onFailure();
        
        // verify
        verify();
        assertTrue("should not be NULL", Transaction.NULL != currentTransaction());
    }
    
    // "RequiresNew" transaction policy
    
	public void testShouldCreateTransactionWhenRequiresNewMethodIsInvoked() throws Exception {
        // setup
		factoryMockWillReturnTransactionMock();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        
        // execute
        handler.onInvoke(Transaction.REQUIRES_NEW);
        
        // verify
        verify();
	}
    
    public void testShouldCommitTransactionWhenRequiresNewMethodSucceeds() throws Exception {
		// setup
        factoryMockWillReturnTransactionMock();
        transactionMock.expects(once()).method(commitUnlessRollbackOnly).withNoArguments();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        handler.onInvoke(Transaction.REQUIRES_NEW);
        
        // execute
        handler.onSuccess();
        
        // verify
        verify();
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldRollbackTransactionWhenRequiresNewMethodFails() throws Exception {
		// setup
        factoryMockWillReturnTransactionMock();
        transactionMock.expects(once()).method(rollback).withNoArguments();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        handler.onInvoke(Transaction.REQUIRES_NEW);
        
        // execute
        handler.onFailure();
        
        // verify
        verify();
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldSuspendExistingTransactionWhenRequiresNewMethodIsInvokedAndRestoreAfterItEnds() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // execute
        handler.onInvoke(Transaction.REQUIRES_NEW);
        handler.onSuccess();
        
        // verify
        verify();
        assertSame(transactionMock.proxy(), currentTransaction());
	}
    
    public void testShouldSuspendExistingTransactionWhenRequiresNewMethodIsInvokedAndRestoreAfterItFails() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);

        // execute
        handler.onInvoke(Transaction.REQUIRES_NEW);
        handler.onFailure();
        
        // verify
        verify();
        assertSame(transactionMock.proxy(), currentTransaction());
	}
    
    // "Supports" transaction policy
    
    public void testShouldNotCreateTransactionIfNoneExistsWhenSupportsMethodIsInvoked() throws Exception {
		// setup
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        
        // expect
        factoryMock.expects(never()).method(createTransaction).withNoArguments();
        
        // execute
        handler.onInvoke(Transaction.SUPPORTS);
        
        // verify
        verify();
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldNotCreateTransactionIfOneAlreadyExistsWhenSupportsMethodIsInvoked() throws Exception {
        // setup
		installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler otherHandler = new XjbTransactionHandler(otherFactory);
        
        // expect
        otherFactoryMock.expects(never()).method(createTransaction).withNoArguments();
        
        // execute
        otherHandler.onInvoke(Transaction.SUPPORTS);
        
        // verify
        verify();
        assertSame(transactionMock.proxy(), currentTransaction());
	}
    
    // "Not Supported" transaction policy
    
    public void testShouldNotCreateTransactionIfNoneExistsWhenNotSupportedMethodIsInvoked() throws Exception {
		// setup
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        
        // expect
        factoryMock.expects(never()).method(createTransaction).withNoArguments();
        
        // execute
        handler.onInvoke(Transaction.NOT_SUPPORTED);
        
        // verify
        verify();
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldSuspendTransactionIfOneExistsWhenNotSupportedMethodIsInvoked() throws Exception {
        // setup
		installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // execute
        handler.onInvoke(Transaction.NOT_SUPPORTED);
        
        // verify
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldRestoreTransactionIfOneExistedWhenNotSupportedMethodSucceeds() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        handler.onInvoke(Transaction.NOT_SUPPORTED);
        
        // execute
        handler.onSuccess();
        
        // verify
        verify();
        assertSame(transactionMock.proxy(), currentTransaction());
	}
    
    public void testShouldRestoreTransactionIfOneExistedWhenNotSupportedMethodFails() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        handler.onInvoke(Transaction.NOT_SUPPORTED);
        
        // execute
        handler.onFailure();
        
        // verify
        verify();
        assertSame(transactionMock.proxy(), currentTransaction());
	}
    
    // "Mandatory" transaction policy
    
    public void testShouldThrowEJBExceptionIfNoTransactionExistsWhenMandatoryMethodIsInvoked() throws Exception {
        // setup
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // execute
        try {
            handler.onInvoke(Transaction.MANDATORY);
            fail("Should have thrown EJBException");
        } catch (EJBException e) {
        }
    }
    
    public void testShouldNotCreateTransactionIfOneExistsWhenMandatoryMethodIsInvoked() throws Exception {
        // setup
		installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler otherHandler = new XjbTransactionHandler(otherFactory);
        
        // expect
        otherFactoryMock.expects(never()).method(createTransaction).withNoArguments();
        
        // execute
        otherHandler.onInvoke(Transaction.MANDATORY);
            
        // verify
        verify();
	}
    
    public void testShouldNotCommitWhenMandatoryMethodSucceeds() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // expect
        transactionMock.expects(never()).method(commitUnlessRollbackOnly).withNoArguments();
        
        // execute
        handler.onInvoke(Transaction.MANDATORY);
        handler.onSuccess();
        
        // verify
        verify();
	}

	public void testShouldNotRollbackWhenMandatoryMethodFails() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // expect
        transactionMock.expects(never()).method(rollback).withNoArguments();
        
        // execute
        handler.onInvoke(Transaction.MANDATORY);
        handler.onFailure();
        
        // verify
        verify();
	}
    
    // "Never" transaction policy
    
    public void testShouldThrowEJBExceptionIfTransactionExistsWhenNeverMethodIsInvoked() throws Exception {
        installMockTransactionAsCurrentTransaction();
        XjbTransactionHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // execute
        try {
            handler.onInvoke(Transaction.NEVER);
            
            // verify
            fail("Should throw EJBException");
        } catch (EJBException e) {
        }
    }
    
    public void testShouldNotCreateTransactionIfNoneExistsWhenNeverMethodIsInvoked() throws Exception {
		// setup
        factoryMock.expects(never()).method(createTransaction).withNoArguments();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        
        // execute
        handler.onInvoke(Transaction.NEVER);
        
        // verify
        verify();
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldNotCommitTransactionWhenNeverMethodSucceeds() throws Exception {
		// setup
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        handler.onInvoke(Transaction.NEVER);
        
        // execute
        handler.onSuccess();
        
        // verify
        verify();
	}
    
    public void testShouldNotRollbackTransactionWhenNeverMethodFails() throws Exception {
		// setup
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        handler.onInvoke(Transaction.NEVER);
        
        // execute
        handler.onFailure();
        
        // verify
        verify();
	}
    
    // Miscellaneous behaviour
    
    public void testShouldRestoreSuspendedTransactionIfCommitThrowsExceptionWhenMethodSucceeds() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        otherFactoryMockWillReturnOtherTransactionMock();
        TransactionPolicyHandler otherHandler = new XjbTransactionHandler(otherFactory);
        
        // expect
        otherTransactionMock.expects(once()).method(commitUnlessRollbackOnly).withNoArguments()
            .will(throwException(new EJBException("oops")));
        
        // execute
        otherHandler.onInvoke(Transaction.REQUIRES_NEW); // first transaction is suspended
        try {
            otherHandler.onSuccess();
            fail("Mock transaction should have thrown EJBException");
        } catch (EJBException expected) {
        }
        
        // verify
        verify();
        assertEquals(transactionMock.proxy(), currentTransaction());
	}
    
    public void testShouldRestoreSuspendedTransactionIfRollbackThrowsExceptionWhenMethodFails() throws Exception {
        // setup
		installMockTransactionAsCurrentTransaction();
        otherFactoryMockWillReturnOtherTransactionMock();
        TransactionPolicyHandler otherHandler = new XjbTransactionHandler(otherFactory);
        
        // expect
        otherTransactionMock.expects(once()).method(rollback).withNoArguments()
            .will(throwException(new EJBException("oops")));
        
        
        // execute
        otherHandler.onInvoke(Transaction.REQUIRES_NEW); // first transaction is suspended
        try {
            otherHandler.onFailure();
            fail("Mock transaction should have thrown EJBException");
        } catch (EJBException expected) {
        }
        
        // verify
        verify();
        assertEquals(transactionMock.proxy(), currentTransaction());
	}
}
