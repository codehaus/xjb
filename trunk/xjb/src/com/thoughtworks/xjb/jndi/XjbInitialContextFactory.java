/*
 * Created on 02-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.jndi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;

import org.apache.log4j.Logger;


public class XjbInitialContextFactory implements JndiRegistry, InitialContextFactory {
    private static final Logger log = Logger.getLogger(XjbInitialContextFactory.class);

    private static final Map localContexts = new HashMap();
    private static final HashMap globalContext = new HashMap(); // we use HashMap.clone()

    private static String localContextName;
    
    private static class ContextInvocationHandler implements InvocationHandler {
        private static final String JAVA_COMP_ENV = "java:comp/env";
        
        private final Map global;
        private final Map local;
        
        public ContextInvocationHandler(HashMap global, HashMap local) {
            this.global = (Map)global.clone();
            this.local = (Map)local.clone();
        }
        
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            if ("lookup".equals(method.getName())) {
                final String jndiName = (String) args[0];
                return lookup(jndiName, proxy);
            }
            throw new UnsupportedOperationException("invoke");
        }

        private Object lookup(String jndiName, Object proxy) throws NamingException {
            // special case
            if (JAVA_COMP_ENV.equals(jndiName)) {
                log.debug("Returning self as java:comp/env subcontext");
                return proxy;
            }
            jndiName = XjbInitialContextFactory.fullyQualified(jndiName);
            Object result = local.get(jndiName);
            if (result == null) {
                result = global.get(jndiName);
            }
            if (result == null) {
                throw new NamingException("Unable to lookup " + jndiName);
            }
            return result;
        }
    }

    /**
     * Install this class as the system-wide initial context factory.
     * 
     * @see NamingManager#setInitialContextFactoryBuilder(javax.naming.spi.InitialContextFactoryBuilder)
     */
    public XjbInitialContextFactory() throws NamingException {
        if (!NamingManager.hasInitialContextFactoryBuilder()) {
            NamingManager.setInitialContextFactoryBuilder(new XjbInitialContextFactoryBuilder());
        }
    }

    /**
     * Reset the registry to its initial state
     * 
     * You can only install a factory builder once, so to reset the JNDI registry
     * you need to {@link #clear()} it down.
     */
    public static void clear() {
        globalContext.clear();
        localContexts.clear();
        localContextName = null;
    }

    /**
     * Push a new local JNDI context for future initial context lookups
     * 
     * @see #getInitialContext(Hashtable)
     */
    public static void setLocalContext(String contextName) {
        localContextName = contextName;
    }
    
    /**
     * Get the name of the current local context
     */
    public static String getLocalContextName() {
        return localContextName;
    }

    /**
     * Provide an <tt>InitialContextFactory</tt> to use to navigate a JNDI
     * tree.
     *
     * @return a dynamic {@link Proxy} representing the <tt>Context</tt>
     */
    public Context getInitialContext(Hashtable environment) throws NamingException {
        log.debug("Getting initial context - current local context = " + localContextName);
        return (Context) Proxy.newProxyInstance(
                Context.class.getClassLoader(),
                new Class[]{Context.class},
                new ContextInvocationHandler(globalContext, getLocalContext(localContextName)));
    }

    /**
     * Register an object against a particular JNDI name in global context
     */
    public void register(String jndiName, Object object) {
        if (object instanceof EJBHome) {
            try {
                EJBHome home = (EJBHome)object;
				String remoteInterfaceName = home.getEJBMetaData().getRemoteInterfaceClass().getName();
                registerCommonGlobalNames(jndiName, remoteInterfaceName, home);
			} catch (RemoteException e) {
                throw new Error("Unexpected error in metadata for " + jndiName + ": " + e.getMessage());
			}
        }
        else {
            globalContext.put(fullyQualified(jndiName), object);
        }
    }

    /**
     * Register an object against a particular JNDI name in local context
     */
    public void register(String contextName, String jndiName, Object object) {
        getLocalContext(contextName).put(fullyQualified(jndiName), object);
    }

    private void registerCommonGlobalNames(String ejbName, String remoteClassName, EJBHome ejbHome) {
        // Generic
        String genericName = "ejb/" + ejbName;
    
        // Orion
        String orionName = ejbName;
        
        // WebSphere
        StringBuffer buf = new StringBuffer();
        char[] remoteChars = remoteClassName.toCharArray();
        for (int i = 0; i < remoteChars.length; i++) {
            buf.append(remoteChars[i] == '.' ? '/' : remoteChars[i]);
        }
        String websphereName = buf.toString();
        
        globalContext.put(fullyQualified(genericName), ejbHome);
        globalContext.put(fullyQualified(orionName), ejbHome);
        globalContext.put(fullyQualified(websphereName), ejbHome);
    }

    private static HashMap getLocalContext(String contextName) {
        HashMap localContext = (HashMap) localContexts.get(contextName);
        if (localContext == null) {
            localContext = new HashMap();
            localContexts.put(contextName, localContext);
        }
        return localContext;
    }
    
    private static String fullyQualified(String jndiName) {
        if (jndiName.startsWith("java:comp/env/")) {
            return jndiName;
        }
        else {
            return "java:comp/env/" + jndiName;
        }
    }
}
