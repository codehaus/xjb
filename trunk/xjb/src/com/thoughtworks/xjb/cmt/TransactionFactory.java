/*
 * Created on 24-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import com.thoughtworks.proxy.toys.nullobject.Null;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public interface TransactionFactory {

	TransactionFactory NULL = (TransactionFactory) Null.object(TransactionFactory.class);

	Transaction createTransaction();
}
