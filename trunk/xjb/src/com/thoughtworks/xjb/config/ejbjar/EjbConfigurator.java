/*
 * Created on 21-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.config.ejbjar;
import javax.naming.NamingException;

import com.thoughtworks.xjb.ejb.HomeFactory;
/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public interface EjbConfigurator {
	boolean STATELESS = HomeFactory.STATELESS;
	boolean STATEFULL = HomeFactory.STATEFUL;
	
	void registerSessionBean(String ejbName, Class homeInterface,
			Class remoteInterface, Class ejbClass, boolean isStateless)
			throws InstantiationException, IllegalAccessException;
    
	void registerEnvEntry(String ejbName, String entryName, Class entryType,
			String entryValue) throws Exception;
    
	void registerResourceRef(String ejbName, String refName, Class resType)
			throws ClassCastException, NamingException;
    
	void registerEjbRef(String ejbName, String jndiName, String targetEjbName);
}