/*
 * Created on 03-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.ejb;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import com.thoughtworks.proxy.factory.StandardProxyFactory;
import com.thoughtworks.proxy.toys.delegate.DelegatingInvoker;
import com.thoughtworks.proxy.toys.delegate.DelegationException;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbHomeFactory implements HomeFactory {
    public static class MethodFinder {
        private Method method;

        public MethodFinder(Object delegate, String methodName, Class[] parameterTypes) {
            try {
                System.out.println("Looking for " + methodName);
                method = delegate.getClass().getMethod(methodName, parameterTypes);
            } catch (Exception e) {
                throw new IllegalArgumentException(methodName);
            }
        }

        public Method method() {
            return method;
        }
    }

    /**
     * Manages all calls to the EJBHome object
     */
    private class SessionHomeInvoker extends DelegatingInvoker {
        private final String ejbName;

        private final Class homeInterface;

        private final Class remoteInterface;

        private final boolean stateful;

        public SessionHomeInvoker(String ejbName, Class homeInterface,
                Class remoteInterface, Object impl, boolean stateful) {
            super(impl);
            this.ejbName = ejbName;
            this.homeInterface = homeInterface;
            this.remoteInterface = remoteInterface;
            this.stateful = stateful;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            EJBHome ejbHome = (EJBHome) proxy;
            if (method.getName().startsWith("create")) {
                return createRemote(ejbHome, method, args);
            } else if ("getEJBMetaData".equals(method.getName())) {
                return new XjbEJBMetaData(ejbHome, homeInterface,
                        remoteInterface, stateful);
            } else if ("toString".equals(method.getName())) {
                return "Home proxy for " + delegate().toString();
            } else {
                throw new UnsupportedOperationException(method.getName());
            }
        }

        private EJBObject createRemote(EJBHome ejbHome, Method ejbCreateMethod,
                Object[] args) throws CreateException, RemoteException,
                Throwable {
            final EJBObject remoteProxy = createRemoteProxy(ejbHome);
            callEjbCreate(ejbCreateMethod, args);
            return remoteProxy;
        }

        private EJBObject createRemoteProxy(EJBHome ejbHome)
                throws CreateException, RemoteException {
            return remoteFactory.createRemote(ejbName, ejbHome,
                    remoteInterface, delegate());
        }

        private void callEjbCreate(Method method, Object[] args)
                throws Throwable {
            final String ejbCreate = "ejbC" + method.getName().substring(1);
            try {
                Method delegateMethod = new MethodFinder(delegate(), ejbCreate,
                        method.getParameterTypes()).method();
                super.invokeOnDelegate(delegateMethod, args);
            } catch (DelegationException e) {
                throw new RemoteException(e.getMessage(), e.getCause());
            }
        }
    }

    private final RemoteFactory remoteFactory;

    public XjbHomeFactory(RemoteFactory remoteFactory) {
        this.remoteFactory = remoteFactory;
    }

    public XjbHomeFactory() {
        this(new XjbRemoteFactory());
    }

    public EJBHome createHome(String ejbName, Class homeInterface,
            Class remoteInterface, Object impl) {
        return createSessionHome(ejbName, homeInterface, remoteInterface, impl,
                true);
    }

    public EJBHome createSessionHome(String ejbName, Class homeInterface,
            Class remoteInterface, Object impl, boolean stateless) {
        return (EJBHome) new StandardProxyFactory().createProxy(
                new Class[] { homeInterface }, new SessionHomeInvoker(ejbName,
                        homeInterface, remoteInterface, impl, stateless));
    }
}
