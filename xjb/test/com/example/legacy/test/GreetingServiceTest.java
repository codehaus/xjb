/*
 * Created on 19-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.example.legacy.test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.TestCase;

import com.example.legacy.ejb.GreetingService;
import com.example.legacy.ejb.GreetingServiceBean;
import com.example.legacy.ejb.GreetingServiceHome;
import com.thoughtworks.xjb.config.ejbjar.XjbEjbConfigurator;

/**
 * This is the example from the two minute tutorial.
 * 
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class GreetingServiceTest extends TestCase {
    public void setUp() throws Exception {
        new XjbEjbConfigurator().registerSessionBean(
                "greetingService",
                GreetingServiceHome.class,
                GreetingService.class,
                GreetingServiceBean.class, true);
    }
    
    public void testShouldSayHello() throws Exception {
        Context context = new InitialContext();
        GreetingServiceHome home = (GreetingServiceHome)PortableRemoteObject.narrow(
                context.lookup("ejb/greetingService"), GreetingServiceHome.class);
        GreetingService service = home.create();
        String result = service.greet();
        service.remove();
		assertEquals("hello", result);
	}
}
