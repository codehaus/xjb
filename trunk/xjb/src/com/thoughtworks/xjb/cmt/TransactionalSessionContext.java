/*
 * Created on 03-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import javax.ejb.SessionContext;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public interface TransactionalSessionContext extends SessionContext {
    void setTransaction(Transaction transaction);
}
