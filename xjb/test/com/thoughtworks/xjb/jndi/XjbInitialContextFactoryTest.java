/*
 * Created on 02-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.jndi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbInitialContextFactoryTest extends TestCase {
    private XjbInitialContextFactory factory;
    
    public void setUp() throws Exception {
        factory = new XjbInitialContextFactory();
    }
    
    public void tearDown() {
        XjbInitialContextFactory.clear();
    }
    
    public void testShouldInstallItselfOnConstruction() throws Exception {
    	assertTrue(NamingManager.hasInitialContextFactoryBuilder()); 
    }
    
    public void testShouldRegisterComponentAgainstJndiName() throws Exception {
        // setup
        Object foo = new Object();
        factory.register("java:comp/env/Foo", foo);
        
        // execute/verify
        final Context initialContext = factory.getInitialContext(null);
        assertSame(foo, initialContext.lookup("java:comp/env/Foo"));
    }
    
    public void testShouldFullyQualifyRelativeJndiNameOnRegister() throws Exception {
        // setup
        Object foo = new Object();
        factory.register("Foo", foo);
        
        // execute/verify
        final Context initialContext = factory.getInitialContext(null);
        assertSame(foo, initialContext.lookup("java:comp/env/Foo"));
    }
    
    public void testShouldFullyQualifyRelativeJndiNameOnLookup() throws Exception {
        // setup
        Object foo = new Object();
        factory.register("java:comp/env/Foo", foo);
        
        // execute/verify
        final Context initialContext = factory.getInitialContext(null);
        assertSame(foo, initialContext.lookup("Foo"));
    }
    
    private void assertThrowsNamingException(Context context, String name) {
        try {
            context.lookup(name);
            fail(name + " should not have been found");
        }
        catch (NamingException e) {
            // expected
        }
    }
    
    public void testShouldThrowNamingExceptionIfLookupFails() throws Exception {
    	 assertThrowsNamingException(new InitialContext(), "no/such/Object");
    }
    
    public void testShouldCreateSubcontextForJavaCompEnv() throws Exception {
        // setup
        Object foo = new Object();
        factory.register("java:comp/env/Foo", foo);
        final Context initialContext = factory.getInitialContext(null);
        
        // execute/validate
        final Context subcontext = (Context)initialContext.lookup("java:comp/env");
        assertNotNull(subcontext);
        assertSame(foo, subcontext.lookup("Foo"));
    }
    
    public void testShouldProvideLocalContext() throws Exception {
        // register defaultObject in default context
        Object globalObject = new Object();
        factory.register("GlobalObject", globalObject);
        
        Object localObject = new Object();
        factory.register("local", "LocalObject", localObject);
        
        // check global context
        Context globalCtx = factory.getInitialContext(null);
        assertSame(globalCtx.lookup("GlobalObject"), globalObject);
        assertThrowsNamingException(globalCtx, "LocalObject");

        // check local context
        XjbInitialContextFactory.setLocalContext("local");
        Context localCtx = factory.getInitialContext(null);
        assertSame(globalObject, localCtx.lookup("GlobalObject"));
        assertSame(localObject, localCtx.lookup("LocalObject"));
    }

    public void testShouldResolveGlobalResourceReference() throws Exception {
        Object resource = new Object();
        
        // register resource ref
        factory.register("jdbc/SomeResource", resource);
        
        // resolve resource ref
        XjbInitialContextFactory.setLocalContext("SomeBean");
        assertSame(resource, new InitialContext().lookup("jdbc/SomeResource"));
    }
    
    public void testShouldClearContexts() throws Exception {
        // setup
        factory.register("Object", new Object());
        factory.register("local", "LocalObject", new Object());

        XjbInitialContextFactory.setLocalContext("local");
        Context before = new InitialContext();
        assertNotNull(before.lookup("Object"));
        assertNotNull(before.lookup("LocalObject"));
        assertNotNull(XjbInitialContextFactory.getLocalContextName());

        // execute
        XjbInitialContextFactory.clear();
        
        // verify
        assertNull(XjbInitialContextFactory.getLocalContextName());
        XjbInitialContextFactory.setLocalContext("local");
        Context after = new InitialContext();
        assertThrowsNamingException(after, "Object");
        assertThrowsNamingException(after, "LocalObject");
    }
    
    public void testShouldProvideSeparateCopyOfRegistryForEachInitialContext() throws Exception {
    	 // setup
        factory.register("Object", new Object());
        factory.register("local", "LocalObject", new Object());
        
        // execute
        XjbInitialContextFactory.setLocalContext("local");
        Context ctx = new InitialContext();
//        XjbInitialContextFactory.clear();
        
        // verify
        assertNotNull(ctx.lookup("Object"));
        assertNotNull(ctx.lookup("LocalObject"));
    }
}
