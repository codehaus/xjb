/*
 * Created on 03-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import com.thoughtworks.xjb.ejb.XjbSessionContext;


/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class CmtSessionContext extends XjbSessionContext {

	private final TransactionGetter transactionGetter;


	public CmtSessionContext(EJBHome home, EJBObject remote, TransactionGetter transactionGetter) {
		super(home, remote);
		this.transactionGetter = transactionGetter;
	}
    
    
	public void setRollbackOnly() throws IllegalStateException {
		try {
			super.setRollbackOnly();
		} finally {
            transactionGetter.getTransaction().setRollbackOnly();
		}
	}
}
