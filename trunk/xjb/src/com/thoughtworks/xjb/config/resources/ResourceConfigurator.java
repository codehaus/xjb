/*
 * Created on 21-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.config.resources;
import java.io.Reader;
import java.rmi.RemoteException;
/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public interface ResourceConfigurator {
	void read(Reader xmlConfig) throws RemoteException;
}