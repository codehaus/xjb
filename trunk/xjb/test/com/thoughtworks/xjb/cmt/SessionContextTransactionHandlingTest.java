/*
 * Created on 01-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import junit.framework.TestCase;

import org.jmock.Mock;
import org.jmock.core.mixin.Invoked;

import com.thoughtworks.xjb.ejb.XjbSessionContext;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class SessionContextTransactionHandlingTest extends TestCase {
    public void testShouldSetRollbackOnly() throws Exception {
        // setup
        Mock transactionMock = new Mock(Transaction.class);
        TransactionalSessionContext context = new XjbSessionContext(null, null);
        context.setTransaction((Transaction) transactionMock.proxy());
        
        // expect
        transactionMock.expects(Invoked.once()).method("setRollbackOnly").withNoArguments();
        
        // execute
        context.setRollbackOnly();
        
        // verify
        transactionMock.verify();
	}
}
