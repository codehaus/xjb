/*
 * Created on 01-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.config.ejbjar;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import com.thoughtworks.xjb.ejb.XjbHomeFactory;
import com.thoughtworks.xjb.jndi.JndiRegistry;
import com.thoughtworks.xjb.jndi.XjbInitialContextFactory;
import com.thoughtworks.xjb.log.Log;
import com.thoughtworks.xjb.log.LogFactory;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbEjbConfigurator implements EjbConfigurator {
    private static final Log log = LogFactory.getLog(XjbEjbConfigurator.class);

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
	private final XjbHomeFactory homeFactory;

    public XjbEjbConfigurator(JndiRegistry jndiRegistry, Context context) throws NamingException {
        this.jndiRegistry = jndiRegistry;
        this.context = context;
        // TODO inject this
		homeFactory = new XjbHomeFactory();
    }
    
    public XjbEjbConfigurator() throws NamingException {
        this(new XjbInitialContextFactory(), new InitialContext());
	}

	/**
     * Create a session bean and register it in the global context
     * under several common JNDI names
     */
	public void registerSessionBean(String ejbName,
            Class homeInterface, Class remoteInterface, Class ejbClass, boolean isStateless)
			throws InstantiationException, IllegalAccessException {
		EJBHome ejbHome = homeFactory.createSessionHome(ejbName,
				homeInterface, remoteInterface, ejbClass.newInstance(),
				isStateless);
		registerCommonGlobalNames(ejbName, remoteInterface.getName(), ejbHome);
		registeredBeans.put(ejbName, ejbHome);
		resolveOutstandingEjbRefs();
	}
    
    private void registerCommonGlobalNames(String ejbName, String remoteClassName, EJBHome ejbHome) {
        // Generic
        String genericName = "ejb/" + ejbName;
    
        // Orion
        String orionName = ejbName;
        
        // WebSphere
        StringBuffer buf = new StringBuffer();
        char[] chars = remoteClassName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            buf.append(chars[i] == '.' ? '/' : chars[i]);
        }
        String websphereName = buf.toString();
        
        jndiRegistry.register(genericName, ejbHome);
        jndiRegistry.register(orionName, ejbHome);
        jndiRegistry.register(websphereName, ejbHome);
    }

    public void registerEnvEntry(String ejbName, String entryName, Class entryType, String entryValue) throws Exception {
        try {
    	        Constructor ctor = entryType.getConstructor(new Class[] {String.class});
                Object value = ctor.newInstance(new Object[] {entryValue});
                jndiRegistry.register(ejbName, entryName, value);
        } catch (InvocationTargetException e) {
        	throw (Exception)e.getTargetException();
        }
    }

    public void registerResourceRef(String ejbName, String refName, Class resType) throws ClassCastException, NamingException {
        log.debug("Checking for " + refName + " for " + ejbName);
        Object resource = PortableRemoteObject.narrow(context.lookup(refName), resType);
        log.debug("found it!");
        jndiRegistry.register(ejbName, refName, resource);
    }

    public void registerEjbRef(String ejbName, String jndiName, String targetEjbName) {
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

    /**
     * Resolve all <tt>&lt;ejb-ref&gt;</tt> EJB references.
     * 
     * This is a second pass after all the <tt>ejb-jar.xml</tt> files have been read
     * using {@link EjbJarParser#read(Reader)}
     */
    private void resolveOutstandingEjbRefs() {
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
