/*
 * Created on 24-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.example.legacy.test;

import junit.framework.TestCase;

import com.example.legacy.client.ExampleActionBean;
import com.example.legacy.ejb.Example;
import com.example.legacy.ejb.ExampleBean;
import com.example.legacy.ejb.ExampleHome;
import com.thoughtworks.xjb.config.ejbjar.EjbConfigurator;
import com.thoughtworks.xjb.config.ejbjar.XjbEjbConfigurator;

/**
 * This example demonstrates hard-coding the EJB configuration and env-entries.
 * 
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class HardCodedTest extends TestCase {
    
    public void setUp() throws Exception {
        EjbConfigurator configurator = new XjbEjbConfigurator();

        configurator.registerEnvEntry("Example", "SOME_VALUE", String.class, "hello");
        
        configurator.registerSessionBean(
                "Example",
                ExampleHome.class,
                Example.class,
                ExampleBean.class,
                EjbConfigurator.STATELESS);
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
