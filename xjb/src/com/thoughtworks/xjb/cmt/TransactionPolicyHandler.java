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
public interface TransactionPolicyHandler {
	TransactionPolicyHandler NULL = (TransactionPolicyHandler) Null.object(TransactionPolicyHandler.class);

	Transaction onInvoke(Policy policy);

	void onSuccess();

	void onFailure();
}
