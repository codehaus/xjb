/*
 * Created on 23-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import javax.ejb.EJBException;

import org.jmock.core.mixin.Invoked;
import org.jmock.core.mixin.Throw;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbTransactionHandlerTest extends TransactionMockingTestCase {
    
	public void testShouldStartWithNoTransaction() throws Exception {
		// execute
        TransactionAccessor handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // verify
		assertEquals(Transaction.NULL, handler.getTransaction());
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
    
    public void testShouldCreateTransactionIfNoneExistsWhenRequiredMethodStarts() throws Exception {
        // setup
        factoryMockWillReturnTransactionMock();
        XjbTransactionHandler handler = new XjbTransactionHandler(factory);
        
        // execute
        handler.beforeMethodStarts(Transaction.REQUIRED);
        Transaction result = handler.getTransaction();
        
        // verify
        verify();
		assertSame(transactionMock.proxy(), result);
    }
    
    public void testShouldCommitTransactionWhenRequiredMethodEnds() throws Exception {
        // setup
        factoryMockWillReturnTransactionMock();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        handler.beforeMethodStarts(Transaction.REQUIRED);
        
        // expect
        transactionMock.expects(Invoked.once()).method(commitUnlessRollbackOnly).withNoArguments();
        
        // execute
        handler.afterMethodEnds();
        
        // verify
        verify();
        TransactionAccessor accessor = new XjbTransactionHandler(TransactionFactory.NULL);
		assertEquals(Transaction.NULL, accessor.getTransaction());
    }
    
    public void testShouldReuseExistingTransactionIfOneExistsWhenRequiredMethodStarts() throws Exception {
        // setup
        factoryMockWillReturnTransactionMock();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        handler.beforeMethodStarts(Transaction.REQUIRED);
        
        // expect
        otherFactoryMock.expects(Invoked.never()).method(createTransaction).withNoArguments();
        
        // execute
        TransactionPolicyHandler anotherHandler = new XjbTransactionHandler(otherFactory);
        anotherHandler.beforeMethodStarts(Transaction.REQUIRED);
        
        // verify
        verify();
	}
    
    public void testShouldNotCommitTransactionWhenRequiredMethodEndsIfThisHandlerDidNotCreateIt() throws Exception {
        // setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler otherHandler = new XjbTransactionHandler(otherFactory);
        
        // expect
        otherTransactionMock.expects(Invoked.never()).method(commitUnlessRollbackOnly).withNoArguments();
        otherFactoryMock.expects(Invoked.never()).method(createTransaction).withNoArguments();
        
        // execute
        otherHandler.beforeMethodStarts(Transaction.REQUIRED);
        otherHandler.afterMethodEnds();
        
        // verify
        verify();
        assertTrue("should not be NULL", Transaction.NULL != currentTransaction());
	}
    
    public void testShouldRollbackTransactionWhenRequiredMethodFails() throws Exception {
		// setup
        factoryMockWillReturnTransactionMock();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        handler.beforeMethodStarts(Transaction.REQUIRED);
        
        // expect
        transactionMock.expects(Invoked.once()).method(rollback).withNoArguments();
        
        // execute
        handler.afterMethodFails();
        
        // verify
        verify();
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldNotRollbackTransactionWhenRequiredMethodFailsIfThisHandlerDidNotCreateIt() throws Exception {
        // setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        handler.beforeMethodStarts(Transaction.REQUIRED);
        
        // expect
        transactionMock.expects(Invoked.never()).method(rollback).withNoArguments();
        
        // execute
        handler.afterMethodFails();
        
        // verify
        verify();
        assertTrue("should not be NULL", Transaction.NULL != currentTransaction());
    }
    
    // "RequiresNew" transaction policy
    
	public void testShouldCreateTransactionWhenRequiresNewMethodStarts() throws Exception {
        // setup
		factoryMockWillReturnTransactionMock();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        
        // execute
        handler.beforeMethodStarts(Transaction.REQUIRES_NEW);
        
        // verify
        verify();
	}
    
    public void testShouldCommitTransactionWhenRequiresNewMethodEnds() throws Exception {
		// setup
        factoryMockWillReturnTransactionMock();
        transactionMock.expects(Invoked.once()).method(commitUnlessRollbackOnly).withNoArguments();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        handler.beforeMethodStarts(Transaction.REQUIRES_NEW);
        
        // execute
        handler.afterMethodEnds();
        
        // verify
        verify();
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldRollbackTransactionWhenRequiresNewMethodFails() throws Exception {
		// setup
        factoryMockWillReturnTransactionMock();
        transactionMock.expects(Invoked.once()).method(rollback).withNoArguments();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        handler.beforeMethodStarts(Transaction.REQUIRES_NEW);
        
        // execute
        handler.afterMethodFails();
        
        // verify
        verify();
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldSuspendExistingTransactionWhenRequiresNewMethodStartsAndRestoreAfterItEnds() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // execute
        handler.beforeMethodStarts(Transaction.REQUIRES_NEW);
        handler.afterMethodEnds();
        
        // verify
        verify();
        assertSame(transactionMock.proxy(), currentTransaction());
	}
    
    public void testShouldSuspendExistingTransactionWhenRequiresNewMethodStartsAndRestoreAfterItFails() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);

        // execute
        handler.beforeMethodStarts(Transaction.REQUIRES_NEW);
        handler.afterMethodFails();
        
        // verify
        verify();
        assertSame(transactionMock.proxy(), currentTransaction());
	}
    
    // "Supports" transaction policy
    
    public void testShouldNotCreateTransactionIfNoneExistsWhenSupportsMethodStarts() throws Exception {
		// setup
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        
        // expect
        factoryMock.expects(Invoked.never()).method(createTransaction).withNoArguments();
        
        // execute
        handler.beforeMethodStarts(Transaction.SUPPORTS);
        
        // verify
        verify();
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldNotCreateTransactionIfOneAlreadyExistsWhenSupportsMethodStarts() throws Exception {
        // setup
		installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler otherHandler = new XjbTransactionHandler(otherFactory);
        
        // expect
        otherFactoryMock.expects(Invoked.never()).method(createTransaction).withNoArguments();
        
        // execute
        otherHandler.beforeMethodStarts(Transaction.SUPPORTS);
        
        // verify
        verify();
        assertSame(transactionMock.proxy(), currentTransaction());
	}
    
    // "Not Supported" transaction policy
    
    public void testShouldNotCreateTransactionIfNoneExistsWhenNotSupportedMethodStarts() throws Exception {
		// setup
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        
        // expect
        factoryMock.expects(Invoked.never()).method(createTransaction).withNoArguments();
        
        // execute
        handler.beforeMethodStarts(Transaction.NOT_SUPPORTED);
        
        // verify
        verify();
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldSuspendTransactionIfOneExistsWhenNotSupportedMethodStarts() throws Exception {
        // setup
		installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // execute
        handler.beforeMethodStarts(Transaction.NOT_SUPPORTED);
        
        // verify
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldRestoreTransactionIfOneExistedWhenNotSupportedMethodEnds() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        handler.beforeMethodStarts(Transaction.NOT_SUPPORTED);
        
        // execute
        handler.afterMethodEnds();
        
        // verify
        verify();
        assertSame(transactionMock.proxy(), currentTransaction());
	}
    
    public void testShouldRestoreTransactionIfOneExistedWhenNotSupportedMethodFails() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        handler.beforeMethodStarts(Transaction.NOT_SUPPORTED);
        
        // execute
        handler.afterMethodFails();
        
        // verify
        verify();
        assertSame(transactionMock.proxy(), currentTransaction());
	}
    
    // "Mandatory" transaction policy
    
    public void testShouldThrowEJBExceptionIfNoTransactionExistsWhenMandatoryMethodStarts() throws Exception {
        // setup
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // execute
        try {
            handler.beforeMethodStarts(Transaction.MANDATORY);
            fail("Should have thrown EJBException");
        } catch (EJBException e) {
        }
    }
    
    public void testShouldNotCreateTransactionIfOneExistsWhenMandatoryMethodStarts() throws Exception {
        // setup
		installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler otherHandler = new XjbTransactionHandler(otherFactory);
        
        // expect
        otherFactoryMock.expects(Invoked.never()).method(createTransaction).withNoArguments();
        
        // execute
        otherHandler.beforeMethodStarts(Transaction.MANDATORY);
            
        // verify
        verify();
	}
    
    public void testShouldNotCommitWhenMandatoryMethodEnds() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // expect
        transactionMock.expects(Invoked.never()).method(commitUnlessRollbackOnly).withNoArguments();
        
        // execute
        handler.beforeMethodStarts(Transaction.MANDATORY);
        handler.afterMethodEnds();
        
        // verify
        verify();
	}

	public void testShouldNotRollbackWhenMandatoryMethodFails() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // expect
        transactionMock.expects(Invoked.never()).method(rollback).withNoArguments();
        
        // execute
        handler.beforeMethodStarts(Transaction.MANDATORY);
        handler.afterMethodFails();
        
        // verify
        verify();
	}
    
    // "Never" transaction policy
    
    public void testShouldThrowEJBExceptionIfTransactionExistsWhenNeverMethodStarts() throws Exception {
        installMockTransactionAsCurrentTransaction();
        XjbTransactionHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        
        // execute
        try {
            handler.beforeMethodStarts(Transaction.NEVER);
            
            // verify
            fail("Should throw EJBException");
        } catch (EJBException e) {
        }
    }
    
    public void testShouldNotCreateTransactionIfNoneExistsWhenNeverMethodStarts() throws Exception {
		// setup
        factoryMock.expects(Invoked.never()).method(createTransaction).withNoArguments();
        TransactionPolicyHandler handler = new XjbTransactionHandler(factory);
        
        // execute
        handler.beforeMethodStarts(Transaction.NEVER);
        
        // verify
        verify();
        assertEquals(Transaction.NULL, currentTransaction());
	}
    
    public void testShouldNotCommitTransactionWhenNeverMethodEnds() throws Exception {
		// setup
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        handler.beforeMethodStarts(Transaction.NEVER);
        
        // execute
        handler.afterMethodEnds();
        
        // verify
        verify();
	}
    
    public void testShouldNotRollbackTransactionWhenNeverMethodFails() throws Exception {
		// setup
        TransactionPolicyHandler handler = new XjbTransactionHandler(TransactionFactory.NULL);
        handler.beforeMethodStarts(Transaction.NEVER);
        
        // execute
        handler.afterMethodFails();
        
        // verify
        verify();
	}
    
    // Miscellaneous behaviour
    
    public void testShouldRestoreSuspendedTransactionIfCommitThrowsExceptionWhenMethodEnds() throws Exception {
		// setup
        installMockTransactionAsCurrentTransaction();
        otherFactoryMockWillReturnOtherTransactionMock();
        TransactionPolicyHandler otherHandler = new XjbTransactionHandler(otherFactory);
        
        // expect
        otherTransactionMock.expects(Invoked.once()).method(commitUnlessRollbackOnly).withNoArguments()
            .will(Throw.exception(new EJBException("oops")));
        
        // execute
        otherHandler.beforeMethodStarts(Transaction.REQUIRES_NEW); // first transaction is suspended
        try {
            otherHandler.afterMethodEnds();
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
        otherTransactionMock.expects(Invoked.once()).method(rollback).withNoArguments()
            .will(Throw.exception(new EJBException("oops")));
        
        
        // execute
        otherHandler.beforeMethodStarts(Transaction.REQUIRES_NEW); // first transaction is suspended
        try {
            otherHandler.afterMethodFails();
            fail("Mock transaction should have thrown EJBException");
        } catch (EJBException expected) {
        }
        
        // verify
        verify();
        assertEquals(transactionMock.proxy(), currentTransaction());
	}
}
