/*
 * Created on 24-Mar-2004
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
public class XjbTransactionHandler implements TransactionPolicyHandler, TransactionAccessor {
    static Transaction transaction = Transaction.NULL;

	private final TransactionFactory factory;
    private boolean createdTransaction = false;

	private Transaction storedTransaction = Transaction.NULL;

	public XjbTransactionHandler(TransactionFactory factory) {
        this.factory = factory;
	}
	
    public Transaction getTransaction() {
        return transaction;
    }
    
    /**
     * Set up the correct transactional context for calling a CMT method
     * 
     * @throws EJBException if preconditions are not satisfied for a particular policy
     */
	public Transaction beforeMethodStarts(Policy policy) {
        checkPreconditions(policy);
        storedTransaction = transaction;

        if (shouldCreateTransaction(policy)) {
            transaction = factory.createTransaction();
            createdTransaction = true;
        }
        else if (shouldSuspendExistingTransaction(policy)) {
            transaction = Transaction.NULL;
        }
        return getTransaction();
    }

    public void afterMethodEnds() {
        finishTransaction(true);
    }

    public void afterMethodFails() {
        finishTransaction(false);
    }
	
	/**
     * Ensure correct transactional context before Mandatory and Never method calls
     * 
     * @throws EJBException if preconditions are not satisfied
     */
	private void checkPreconditions(Policy policy) {
		if (policy == Transaction.MANDATORY && transaction == Transaction.NULL) {
            throw new EJBException("Transaction should exist before Mandatory method call");
        }
        if (policy == Transaction.NEVER && transaction != Transaction.NULL) {
            throw new EJBException("Transaction should not exist before Never method call");
        }
	}

    private boolean shouldCreateTransaction(Policy policy) {
        return (policy == Transaction.REQUIRED && transaction == Transaction.NULL)
            || policy == Transaction.REQUIRES_NEW;
    }

    private boolean shouldSuspendExistingTransaction(Policy policy) {
        return policy == Transaction.NOT_SUPPORTED && transaction != Transaction.NULL;
    }

	private void finishTransaction(boolean succeeded) {
		try {
			if (createdTransaction) {
				if (succeeded) {
                    transaction.commitUnlessRollbackOnly();
                }
                else {
                    transaction.rollback();
                }
			}
		} finally {
            transaction = storedTransaction;
		}
	}
}
