/*
 * Created on 02-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.jndi;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
class XjbInitialContextFactoryBuilder implements InitialContextFactoryBuilder {
    private static final Logger log = Logger.getLogger(XjbInitialContextFactoryBuilder.class);

    public XjbInitialContextFactoryBuilder() {
        log.debug("Constructing factory");
    }

    public InitialContextFactory createInitialContextFactory(Hashtable environment) throws NamingException {
        log.debug("Can we build it, yes we can!");
        return new XjbInitialContextFactory();
    }
}
