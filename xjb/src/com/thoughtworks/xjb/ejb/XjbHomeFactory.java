/*
 * Created on 03-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.ejb;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import com.thoughtworks.proxytoys.DelegatingProxy;
import com.thoughtworks.xjb.cmt.PolicyLookup;
import com.thoughtworks.xjb.cmt.TransactionPolicyHandler;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbHomeFactory implements HomeFactory {
    /**
     * Manages all calls to the EJBHome object
     */
    private class HomeInvocationHandler extends DelegatingProxy.DelegatingInvocationHandler {
        private final String ejbName;
        private final Class homeInterface;
        private final Class remoteInterface;
        private final boolean stateful;
		private final PolicyLookup lookup;
		private final TransactionPolicyHandler handler;
        
        public HomeInvocationHandler(String ejbName, Class homeInterface, Class remoteInterface, Object impl, boolean stateful, PolicyLookup lookup, TransactionPolicyHandler handler) {
            super(impl);
            this.ejbName = ejbName;
            this.homeInterface = homeInterface;
            this.remoteInterface = remoteInterface;
            this.stateful = stateful;
            this.lookup = lookup;
            this.handler = handler;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            EJBHome ejbHome = (EJBHome)proxy;
            if (method.getName().startsWith("create")) {
                return createRemote(ejbHome, method, args);
            }
            else if ("getEJBMetaData".equals(method.getName())) {
                return new XjbEJBMetaData(ejbHome, homeInterface, remoteInterface, stateful);
            }
            else if ("toString".equals(method.getName())) {
                return "Home proxy for " + delegate.toString();
            }
            else {
                throw new UnsupportedOperationException(method.getName());
            }
        }

        private EJBObject createRemote(EJBHome ejbHome, Method method, Object[] args) throws CreateException, RemoteException, Throwable {
            final EJBObject remoteProxy = createRemoteProxy(ejbHome);
            callEjbCreate(method, args);
            return remoteProxy;
        }
        
        private EJBObject createRemoteProxy(EJBHome ejbHome) throws CreateException, RemoteException {
            return remoteFactory.createRemote(ejbName, ejbHome, remoteInterface, delegate, lookup, handler);
        }
        
        private void callEjbCreate(Method method, Object[] args) throws Throwable {
            final String ejbCreate = "ejbC" + method.getName().substring(1);
            invokeOnDelegate(ejbCreate, method.getParameterTypes(), args);
        }
    }

    private final RemoteFactory remoteFactory;
    
    public XjbHomeFactory(RemoteFactory remoteFactory) {
        this.remoteFactory = remoteFactory;
    }
    
    public XjbHomeFactory() {
        this(new XjbRemoteFactory());
    }
    
    public EJBHome createHome(String ejbName, Class homeInterface, Class remoteInterface, Object impl) {
        return createSessionBeanHome(ejbName, homeInterface, remoteInterface, impl, true);
    }
    
    public EJBHome createSessionBeanHome(String ejbName, Class homeInterface, Class remoteInterface, Object impl, boolean stateless) {
        return createHome(ejbName, homeInterface, remoteInterface, impl, stateless, PolicyLookup.NULL, TransactionPolicyHandler.NULL);
    }

	public EJBHome createHome(String ejbName, Class homeInterface, Class remoteInterface, Object impl, boolean stateless, PolicyLookup lookup, TransactionPolicyHandler handler) {
        return (EJBHome)Proxy.newProxyInstance(
                homeInterface.getClassLoader(),
                new Class[] {homeInterface},
                new HomeInvocationHandler(ejbName, homeInterface, remoteInterface, impl, stateless, lookup, handler));
	}
}
