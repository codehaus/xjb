/*
 * Created on 24-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.example.legacy.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.example.legacy.ejb.Example;
import com.example.legacy.ejb.ExampleHome;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class ExampleActionBean  {
    
    public String getSomeValue() throws Exception {
        // get an initial context
        Context context = new InitialContext(null);
        
        // look up an EJBHome the proper way
        ExampleHome home = (ExampleHome) PortableRemoteObject.narrow(
                context.lookup("Example"), ExampleHome.class);
        
        // get a remote reference
        Example example = home.create();
        
        // delegate the call to the EJB
        try {
			return example.getSomeValue();
        } finally {
            // return bean to pool
            example.remove();
		}
    }
}
