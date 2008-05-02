/*
 * Created on 04-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public abstract class TransactionMockingTestCase extends MockObjectTestCase {

	protected static final String createTransaction = "createTransaction";
    protected static final String commitUnlessRollbackOnly = "commitUnlessRollbackOnly";
    protected static final String rollback = "rollback";
    
	protected Mock transactionMock;
	protected Mock factoryMock;
	protected TransactionFactory factory;
	protected Mock otherTransactionMock;
	protected Mock otherFactoryMock;
	protected TransactionFactory otherFactory;

	public void setUp() throws Exception {
	    transactionMock = new Mock(Transaction.class);
	
	    factoryMock = new Mock(TransactionFactory.class);
	    factory = (TransactionFactory) factoryMock.proxy();
	
	    otherFactoryMock = new Mock(TransactionFactory.class);
	    otherFactory = (TransactionFactory) otherFactoryMock.proxy();
	
	    otherTransactionMock = new Mock(Transaction.class);
	    
	    // hacktag
	    XjbTransactionHandler.transaction = Transaction.NULL;
	}

	/**
	 * Set up <tt>factoryMock</tt> to return <tt>transactionMock</tt> for
	 * <tt>factory.getTransaction()</tt>
	 */
	protected void factoryMockWillReturnTransactionMock() {
	    factoryMock.expects(once()).method(createTransaction).withNoArguments()
	        .will(returnValue(transactionMock.proxy()));
	}

	/** Get current singleton (ThreadLocal in real life) transaction */
	protected Transaction currentTransaction() {
	    return new XjbTransactionHandler(TransactionFactory.NULL).getTransaction();
	}

	protected void installMockTransactionAsCurrentTransaction() {
	    factoryMockWillReturnTransactionMock();
	    new XjbTransactionHandler(factory).onInvoke(Transaction.REQUIRED);
	}

	protected void otherFactoryMockWillReturnOtherTransactionMock() {
	    otherFactoryMock.expects(once()).method(createTransaction).withNoArguments()
	        .will(returnValue(otherTransactionMock.proxy()));
	}
}
