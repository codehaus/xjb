/*
 * Created on 02-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.jndi;

import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbInitialContextFactoryBuilderTest extends TestCase {
    
    public void testShouldBuildFakeInitialContextFactory() throws Exception {
        InitialContextFactoryBuilder builder = new XjbInitialContextFactoryBuilder();
        InitialContextFactory factory = builder.createInitialContextFactory(null);
        assertTrue(factory instanceof XjbInitialContextFactory);
    }
}
