/*
 * Created on 29-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import com.thoughtworks.nothing.Null;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public interface TransactionAccessor {
    TransactionAccessor NULL = (TransactionAccessor) Null.object(TransactionAccessor.class);

	Transaction getTransaction();
}
