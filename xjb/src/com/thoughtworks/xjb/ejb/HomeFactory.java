/*
 * Created on 02-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.ejb;
import javax.ejb.EJBHome;
/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public interface HomeFactory {
	public static final boolean STATELESS = true;
	public static final boolean STATEFUL = false;
    
	EJBHome createHome(String ejbName, Class homeInterface,
			Class remoteInterface, Object impl);
    
	EJBHome createSessionHome(String ejbName, Class homeInterface,
			Class remoteInterface, Object impl, boolean stateless);
}
