/*
 * Created on 02-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.ejb;
import javax.ejb.EJBHome;
import com.thoughtworks.xjb.cmt.PolicyLookup;
import com.thoughtworks.xjb.cmt.TransactionPolicyHandler;
/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public interface HomeFactory {
	public static final boolean STATELESS = true;
	public static final boolean STATEFUL = false;
    
	EJBHome createHome(String ejbName, Class homeInterface,
			Class remoteInterface, Object impl);
    
	EJBHome createSessionBeanHome(String ejbName, Class homeInterface,
			Class remoteInterface, Object impl, boolean stateless);
    
	EJBHome createHome(String ejbName, Class homeInterface,
			Class remoteInterface, Object impl, boolean stateless,
			PolicyLookup lookup, TransactionPolicyHandler handler);
}
