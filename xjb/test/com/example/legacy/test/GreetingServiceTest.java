/*
 * Created on 19-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.example.legacy.test;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.TestCase;

import com.example.legacy.ejb.GreetingService;
import com.example.legacy.ejb.GreetingServiceBean;
import com.example.legacy.ejb.GreetingServiceHome;
import com.thoughtworks.xjb.ejb.XjbHomeFactory;
import com.thoughtworks.xjb.jndi.XjbInitialContextFactory;

/**
 * This is the example from the two minute tutorial.
 * 
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class GreetingServiceTest extends TestCase {
    public void setUp() throws Exception {
        EJBHome home = new XjbHomeFactory().createHome(
                "greetingService",
                GreetingServiceHome.class,
                GreetingService.class,
                new GreetingServiceBean());
        new XjbInitialContextFactory().register("ejb/greetingService", home);
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
