/*
 * Created on 24-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.example.legacy.test;

import javax.ejb.EJBHome;

import junit.framework.TestCase;

import com.example.legacy.client.ExampleActionBean;
import com.example.legacy.ejb.Example;
import com.example.legacy.ejb.ExampleBean;
import com.example.legacy.ejb.ExampleHome;
import com.thoughtworks.xjb.ejb.HomeFactory;
import com.thoughtworks.xjb.ejb.XjbHomeFactory;
import com.thoughtworks.xjb.jndi.JndiRegistry;
import com.thoughtworks.xjb.jndi.XjbInitialContextFactory;

/**
 * This example demonstrates hard-coding the EJB configuration and env-entries.
 * 
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class HardCodedTest extends TestCase {
    
    public void setUp() throws Exception {
        JndiRegistry registry = new XjbInitialContextFactory();
        
        // register environment value
        registry.register("Example", "SOME_VALUE", "hello");
        
        EJBHome home = new XjbHomeFactory().createHome(
                "Example",
                ExampleHome.class,
                Example.class,
                new ExampleBean(),
                HomeFactory.STATELESS);
		registry.register("Example", home); // Registered as "java:comp/env/Example"
    }
    
    public void testShouldCallEjbMethodFromClient() throws Exception {
        // setup
        ExampleActionBean client = new ExampleActionBean();
        
        // execute
        String result = client.getSomeValue();
        
        // verify
		assertEquals("hello", result);
    }
}
