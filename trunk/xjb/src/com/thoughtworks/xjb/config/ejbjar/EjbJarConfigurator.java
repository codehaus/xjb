/*
 * Created on 06-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.config.ejbjar;

import java.io.Reader;
import java.rmi.RemoteException;

/**
 * Reads and configures the test framework from one or more <tt>ejb-jar.xml</tt> files.
 * 
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public interface EjbJarConfigurator {
    void read(Reader in) throws RemoteException;
    void read(String url) throws RemoteException;
}
