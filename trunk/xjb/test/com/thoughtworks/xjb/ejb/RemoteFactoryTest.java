/*
 * Created on 10-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import com.thoughtworks.xjb.jndi.JndiRegistry;
import com.thoughtworks.xjb.jndi.XjbInitialContextFactory;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class RemoteFactoryTest extends MockObjectTestCase {
    private static final String ejbRemove = "ejbRemove";
    
	private Object globalObject;
    private Object localObject;
    private SimpleBean simpleBean;

    // the ejb classes
    public interface Simple extends EJBObject {
        void doSomething() throws Exception;
    }
    
    public static class SimpleBean extends SessionBeanSupport {
        public Object local = null;
        public Object global = null;
        
        public void doSomething() throws Exception {
            Context ctx = new InitialContext();
            local = ctx.lookup("localObject");
            global = ctx.lookup("globalObject");
        }
    }
    
    public void setUp() throws Exception {
        JndiRegistry registry = new XjbInitialContextFactory();
		registry.register("localContext", "localObject", localObject = new Object());
		
		// global context
		registry.register("globalObject", globalObject = new Object());
		
		// set up EJB implementation
		simpleBean = new SimpleBean();
    }
    
    public void tearDown() {
        XjbInitialContextFactory.setLocalContext(null);
    }
    
    public interface BeanWithEjbRemove {
        void ejbRemove();
    }
    
    public Simple createRemote(String ejbName, Class remoteInterface, Object impl) {
        return (Simple)new XjbRemoteFactory().createRemote(ejbName, null, remoteInterface, impl);
    }

    public void testShouldCallEjbRemoveWhenRemoveIsInvoked() throws Exception {
        // setup
        Mock mock = new Mock(BeanWithEjbRemove.class);
        
        Simple simple = createRemote("Simple", Simple.class, mock.proxy());
        
        // expect
        mock.expects(once()).method(ejbRemove).withNoArguments().isVoid();
        
        // execute
        simple.remove();
        
        // verify
        mock.verify();
    }
    
    public interface SimpleHome extends EJBHome {
        Simple create() throws CreateException, RemoteException;
    }
    
    public void testShouldGetEJBHome() throws Exception {
         SimpleHome home = (SimpleHome) new XjbHomeFactory().createHome(
                "simple",
                SimpleHome.class,
                Simple.class,
                new SimpleBean());
         Simple simple = home.create();
         assertSame(home, simple.getEJBHome());
    }
    
    public void testShouldBeIdenticalToItself() throws Exception {
        // setup
    	Simple simple = createRemote("Simple", Simple.class, new SimpleBean());
        
        // execute
        boolean result = simple.isIdentical(simple);
        
        // verify
		assertTrue(result);
    }
    
    public void testShouldNotBeIdenticalToAnotherBean() throws Exception {
        Simple one = createRemote("Simple", Simple.class, new SimpleBean());
        Simple another = createRemote("Simple", Simple.class, new SimpleBean());
        assertFalse(one.isIdentical(another));
        assertFalse(another.isIdentical(one));
    }

    public void testShouldSetLocalContextBeforeCallingDelegateMethod() throws Exception {
        // create remote
        Simple remote = createRemote("localContext", Simple.class, simpleBean);
        
        // call method that looks up local env var
        remote.doSomething();
        
        assertSame(globalObject, simpleBean.global);
        assertSame(localObject, simpleBean.local);
    }
    
    public interface AnotherRemoteInterface extends Simple {
        void doSomething() throws Exception;
    }
    
    public class AnotherImpl extends SessionBeanSupport {
        public Object local = null;
        public Object global = null;
        
        public void doSomething() throws Exception {
            Context ctx = new InitialContext();
            // get objects before chaining to the other bean
            local = ctx.lookup("localObject");
            global = ctx.lookup("globalObject");
            
            // chain to the other bean
            Simple remote = createRemote("localContext", Simple.class, simpleBean);
            remote.doSomething();
            
            // assert the contexts have been restored
            assertSame(local, ctx.lookup("localObject"));
            assertSame(global, ctx.lookup("globalObject"));
        }
    }
    
    public void testShouldRecursivelyApplyLocalContextWhenBeanCallsAnotherBean() throws Exception {
        Object anotherLocalObject = new Object();
        JndiRegistry factory = new XjbInitialContextFactory();
        factory.register("anotherLocalContext", "localObject", anotherLocalObject);
        
        AnotherImpl impl = new AnotherImpl();
        
        AnotherRemoteInterface remote =
            (AnotherRemoteInterface) createRemote(
                    "anotherLocalContext",
                    AnotherRemoteInterface.class,
                    impl);
        
        remote.doSomething();
        assertSame(anotherLocalObject, impl.local);
        assertSame(globalObject, impl.global);
    }
}
