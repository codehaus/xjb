/*
 * Created on 02-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.ejb;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import com.thoughtworks.xjb.cmt.PolicyLookup;
import com.thoughtworks.xjb.cmt.TransactionPolicyHandler;
/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public interface RemoteFactory {
	EJBObject createRemote(String ejbName, Class remoteInterface,
			EJBHome ejbHome, Object impl);
	EJBObject createRemote(String ejbName, Class remoteInterface,
			EJBHome ejbHome, Object impl, PolicyLookup policyLookup,
			TransactionPolicyHandler handler);
}