/*
 * Created on 30-Jan-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jmock.Mock;
import org.jmock.core.mixin.Invoked;

import com.thoughtworks.xjb.jndi.JndiRegistry;
import com.thoughtworks.xjb.jndi.XjbInitialContextFactory;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class HomeFactoryTest extends TestCase {
    
	public interface Simple extends EJBObject {
        String getSomething() throws Exception;
    }
    
    public interface SimpleHome extends EJBHome {
        Simple create() throws CreateException, RemoteException;
    }
    
    public static class SimpleBean extends SessionBeanSupport {
        public String getSomething() {
            return "something";
        }
    }
    
    public void testShouldUseSuppliedRemoteFactory() throws Exception {
		// setup
		Mock remoteFactoryMock = new Mock(RemoteFactory.class);
        HomeFactory homeFactory = new XjbHomeFactory((RemoteFactory) remoteFactoryMock.proxy());
        
		// expect
        remoteFactoryMock.expects(Invoked.once()).method("createRemote").withAnyArguments();
        
		// execute
        SimpleHome home = (SimpleHome) homeFactory.createHome("simple", SimpleHome.class, Simple.class, null);
        home.create();
        
		// verify
        remoteFactoryMock.verify();
	}
    
    public void testShouldCreateRemoteProxyToBeanImpl() throws Exception {
        SimpleBean bean = new SimpleBean();
        SimpleHome home = (SimpleHome) createHomeProxy(SimpleHome.class, Simple.class, bean);
        Simple remote = (home).create();
        assertEquals("something", remote.getSomething());
    }
    
    public static class BeanWithEjbCreate extends SessionBeanSupport {
        public boolean ejbCreateWasCalled = false;

        public void ejbCreate() {
            ejbCreateWasCalled = true;
        }
    }
    
    public void testShouldCallEjbCreateWhenCreateCalledOnHomeInterface() throws Exception {
        // setup/execute
        final BeanWithEjbCreate bean = new BeanWithEjbCreate();
        SimpleHome home = (SimpleHome) createHomeProxy(SimpleHome.class, Simple.class, bean);
        home.create();
        
        // verify
        assertTrue(bean.ejbCreateWasCalled);
    }

    public static class FailingEjbCreateBean extends SessionBeanSupport {
        public void ejbCreate() {
            throw new RuntimeException("Oh dear");
        }
    }

    public void testShouldPropagateExceptionFromEjbCreate() throws Exception {
        try {
            // setup/execute
            SimpleHome home = (SimpleHome) createHomeProxy(
                    SimpleHome.class,
                    Simple.class,
                    new FailingEjbCreateBean());
            home.create();

            // verify
            fail();
        } catch (RuntimeException e) {
            // expected
        }
    }

    public static class InaccessibleEjbCreateImpl {
        protected void ejbCreate() {
        }
    }

    public void testShouldThrowRemoteExceptionIfEjbCreateMethodIsNotAccessible()
            throws Exception {
        try {
            // setup/execute
            SimpleHome home = (SimpleHome) createHomeProxy(
                    SimpleHome.class,
                    Simple.class,
                    new InaccessibleEjbCreateImpl());
            home.create();
            
            // verify
            fail();
        } catch (RemoteException e) {
            // expected
        }
    }
    
    public interface MultipleCreateHome extends EJBHome {
        Simple create() throws CreateException, RemoteException;
        Simple create(String s) throws CreateException, RemoteException;
    }
    
    public static class MultipleCreateImpl extends SessionBeanSupport {
        public boolean ejbCreateWasCalled = false;
        
        public void ejbCreate() {
            fail("should not be called");
        }
        
        public void ejbCreate(String s) {
            assertEquals("hello", s);
            ejbCreateWasCalled = true;
        }
    }
    
    public void testShouldCallEjbCreateMethodWithMatchingSignature() throws Exception {
    	// setup
        MultipleCreateImpl impl = new MultipleCreateImpl();
        Class homeType = MultipleCreateHome.class;
		Class remoteType = Simple.class;
		MultipleCreateHome home = (MultipleCreateHome) createHomeProxy(homeType, remoteType, impl);
        
        // execute
        Simple remote = home.create("hello");
        
        // verify
        assertNotNull(remote);
        assertTrue(impl.ejbCreateWasCalled);
    }
    
    private EJBHome createHomeProxy(Class homeType, Class remoteType, Object impl) {
		return new XjbHomeFactory().createHome(null, homeType, remoteType, impl);
	}

	public static class SetSessionContextImpl extends SessionBeanSupport {
        public SessionContext context = null;
        
        public void setSessionContext(SessionContext context) {
            this.context = context;
        }
        
        public void ejbCreate() {
            assertNotNull(context);
        }
    }
    
    public void testShouldCallSetSessionContextBeforeEjbCreate() throws Exception {
        // setup
        final SetSessionContextImpl impl = new SetSessionContextImpl();
        
        // execute
        SimpleHome home = (SimpleHome) createHomeProxy(
                SimpleHome.class,
                Simple.class,
                impl);
        Simple remote = home.create();
        
        // verify
        assertSame(home, impl.context.getEJBHome());
        assertSame(remote, impl.context.getEJBObject());
    }
    
    public static class ImplWithCustomCreateMethod extends SessionBeanSupport {
        public int height = 0;
        
        public void ejbCreateWithHeight(int h) {
            this.height = h;
        }
    }
    
    public interface HomeWithCustomCreateMethod extends EJBHome {
        Simple createWithHeight(int height);
    }
    
    public void testShouldCallAppropriateEjbCreateMethodForCreateXxxMethods()
            throws Exception {
        // setup
        final ImplWithCustomCreateMethod impl = new ImplWithCustomCreateMethod();
        HomeWithCustomCreateMethod home =
            (HomeWithCustomCreateMethod) createHomeProxy(
                    HomeWithCustomCreateMethod.class,
                    Simple.class,
                    impl);
        
        // execute
        home.createWithHeight(170);
        
        // verify
        assertEquals(170, impl.height);
    }
    
    public void testShouldSupportEJBMetaData() throws Exception {
    	 EJBHome home = createHomeProxy(SimpleHome.class, Simple.class, new SimpleBean());
         EJBMetaData metadata = home.getEJBMetaData();
         
         // EJBMetaData contract
         assertTrue("serializable", metadata instanceof Serializable);
         assertEquals("home class", SimpleHome.class, metadata.getHomeInterfaceClass());
         assertEquals("remote class", Simple.class, metadata.getRemoteInterfaceClass());
         assertSame("EJBHome", home, metadata.getEJBHome());
         assertNull("primary key class", metadata.getPrimaryKeyClass());
         assertTrue("is session", metadata.isSession());
         assertTrue("is stateless session", metadata.isStatelessSession());
    }
    
    public void testShouldSupportEJBMetaDataForStatefulSessionBean() throws Exception {
        EJBHome home = new XjbHomeFactory().createHome("Simple", SimpleHome.class, Simple.class, new SimpleBean(), false);
        EJBMetaData meta = home.getEJBMetaData();
        assertTrue("is session", meta.isSession());
        assertTrue("is not stateless session", !meta.isStatelessSession());
    }
    
    public static class LocalContextImpl extends SessionBeanSupport {
        public String getSomething() throws Exception {
           return (String) new InitialContext().lookup("LocalName"); 
        }
    }
    
    public void testShouldConstructRemoteWithCorrectLocalContext() throws Exception {
        JndiRegistry factory = new XjbInitialContextFactory();
        factory.register("localContext", "LocalName", "something");
        
        SimpleHome home =
            (SimpleHome) new XjbHomeFactory().createHome(
                "localContext",
                SimpleHome.class,
                Simple.class,
                new LocalContextImpl());
                
    	assertEquals("something", home.create().getSomething());
    }
}
