/*
 * Created on 01-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.config.ejbjar;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;

import com.thoughtworks.xjb.ejb.XjbHomeFactory;
import com.thoughtworks.xjb.jndi.JndiRegistry;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public abstract class EjbJarConfiguratorSupport implements EjbJarConfigurator {
    private static final Logger log = Logger.getLogger(EjbJarConfiguratorSupport.class);

    /** Represents an <ejb-ref> element */
    private static class EjbRef {
        public final String ejbName;
        public final String jndiName;
        public final String targetEjbName;
        
        public EjbRef(String ejbName, String jndiName, String targetEjbName) {
            this.ejbName = ejbName;
            this.jndiName = jndiName;
            this.targetEjbName = targetEjbName;
        }
    }

    private final Map registeredBeans = new HashMap();
    private final List unresolvedEjbRefs = new ArrayList();
    protected final JndiRegistry jndiRegistry;
	private final Context context;

    protected EjbJarConfiguratorSupport(JndiRegistry jndiRegistry, Context context) throws NamingException {
        this.jndiRegistry = jndiRegistry;
        this.context = context;
    }
    
    /**
     * Read an <tt>ejb-jar.xml</tt> deployment descriptor file.
     * 
     * @throws RemoteException if anything goes wrong
     */
    public abstract void read(Reader in) throws RemoteException;
    
    /**
     * Convenience method to read an <tt>ejb-jar.xml</tt> file from a URL.
     * <br>
     * The URL is resolved into a {@link Reader} using the current classloader.
     * 
     * @throws RemoteException if anything goes wrong
     */
    public void read(String url) throws RemoteException {
        read(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(url)));
    }

    protected void registerSessionBean(String ejbName, Class homeInterface, Class remoteInterface, Class ejbClass, boolean isStateless) throws InstantiationException, IllegalAccessException {
        EJBHome home = new XjbHomeFactory().createHome(
                ejbName,
                homeInterface, remoteInterface,
                ejbClass.newInstance(), isStateless);
        registerCommonGlobalNames(ejbName, remoteInterface.getName(), home);
        registeredBeans.put(ejbName, home);
        resolveOutstandingEjbRefs();
    }

    protected void registerEnvEntry(String ejbName, String entryName, Class entryType, String entryValue) throws Exception {
        try {
    	        Constructor ctor = entryType.getConstructor(new Class[] {String.class});
                Object value = ctor.newInstance(new Object[] {entryValue});
                jndiRegistry.register(ejbName, entryName, value);
        } catch (InvocationTargetException e) {
        	throw (Exception)e.getTargetException();
        }
    }

    protected void registerResourceRef(String ejbName, String refName, Class resType) throws ClassCastException, NamingException {
        log.debug("Checking for " + refName + " for " + ejbName);
        Object resource = PortableRemoteObject.narrow(context.lookup(refName), resType);
        log.debug("found it!");
        jndiRegistry.register(ejbName, refName, resource);
    }

    protected void registerEjbRef(String ejbName, String jndiName, String targetEjbName) {
        EJBHome targetHome = (EJBHome)registeredBeans.get(targetEjbName);
        if (targetHome != null) {
            log.debug("Registering " + targetHome + " as " + jndiName + " for " + ejbName);
            jndiRegistry.register(ejbName, jndiName, targetHome);
        }
        else {
            log.debug("Unresolved target ejb " + targetEjbName + " as " + jndiName + " for " + ejbName);
            unresolvedEjbRefs.add(new EjbRef(ejbName, jndiName, targetEjbName));
        }
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
        
        jndiRegistry.register(genericName, ejbHome);
        jndiRegistry.register(orionName, ejbHome);
        jndiRegistry.register(websphereName, ejbHome);
    }

    /**
     * Resolve all <tt>&lt;ejb-ref&gt;</tt> EJB references.
     * 
     * This is a second pass after all the <tt>ejb-jar.xml</tt> files have been read
     * using {@link #read(Reader)}
     */
    protected void resolveOutstandingEjbRefs() {
        for (Iterator i = unresolvedEjbRefs.iterator(); i.hasNext(); ) {
            EjbRef ejbRef = (EjbRef) i.next();
            Object targetHome = registeredBeans.get(ejbRef.targetEjbName);
            if (targetHome != null) {
                log.debug("Registering " + targetHome + " as " + ejbRef.jndiName + " for " + ejbRef.ejbName);
                jndiRegistry.register(ejbRef.ejbName, ejbRef.jndiName, targetHome);
                i.remove();
            }
        }
    }
}
