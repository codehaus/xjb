/*
 * Created on 06-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.config.ejbjar.exml;

import java.io.Reader;
import java.rmi.RemoteException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.thoughtworks.xjb.config.ejbjar.EjbJarConfiguratorSupport;
import com.thoughtworks.xjb.jndi.JndiRegistry;
import com.thoughtworks.xjb.jndi.XjbInitialContextFactory;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;

/**
 * Reads and configures the test framework from one or more <tt>ejb-jar.xml</tt> files.
 * 
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class ExmlEjbJarConfigurator extends EjbJarConfiguratorSupport {
    private static final Logger log = Logger.getLogger(ExmlEjbJarConfigurator.class);

    public ExmlEjbJarConfigurator(JndiRegistry jndiRegistry, Context context) throws NamingException {
        super(jndiRegistry, context);
    }

    public ExmlEjbJarConfigurator() throws NamingException {
        this(new XjbInitialContextFactory(), new InitialContext());
    }
    
    public void read(Reader in) throws RemoteException {
        try {
            Element enterpriseBeans = new Document(in).getRoot().getElement("enterprise-beans");
            for (Elements sessions = enterpriseBeans.getElements("session"); sessions.hasMoreElements();) {
                Element session = sessions.next();
                parseSessionBean(session);
                parseEnvEntries(session);
                parseResourceRefs(session);
                parseEjbRefs(session);
            }
        }
        catch (Exception e) {
            throw new RemoteException("Unable to parse xml", e);
        }
    }

    private void parseSessionBean(Element session) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String ejbName = session.getTextString("ejb-name");
        // get details
        Class homeClass = getClass(session, "home");
        Class remoteClass = getClass(session, "remote");
        Class ejbClass = getClass(session, "ejb-class");
        String sessionType = session.getTextString("session-type");
        boolean isStateless = "Stateless".equals(sessionType);
        
        registerSessionBean(ejbName, homeClass, remoteClass, ejbClass, isStateless);
    }
    
    private Class getClass(Element session, String tag) throws ClassNotFoundException {
        return Class.forName(session.getTextString(tag));
    }

    private void parseEnvEntries(Element session) throws Exception {
        String ejbName = session.getTextString("ejb-name");
        
        for (Elements entries = session.getElements("env-entry"); entries.hasMoreElements();) {
            Element entry = entries.next();
            String entryName = entry.getTextString("env-entry-name");
            log.debug("Processing " + entryName);
            Class entryType = getClass(entry, "env-entry-type");
            String stringValue = entry.getTextString("env-entry-value");
            registerEnvEntry(ejbName, entryName, entryType, stringValue);
        }
    }

    private void parseResourceRefs(Element session) throws ClassNotFoundException, ClassCastException, NamingException {
        String ejbName = session.getTextString("ejb-name");
        
        for (Elements refs = session.getElements("resource-ref"); refs.hasMoreElements();) {
            Element ref = refs.next();
            String refName = ref.getTextString("res-ref-name");
            Class resType = getClass(ref, "res-type");
            registerResourceRef(ejbName, refName, resType);
        }
    }
    
    private void parseEjbRefs(Element session) throws RemoteException {
        String ejbName = session.getTextString("ejb-name");
        
        for (Elements ejbRefs = session.getElements("ejb-ref"); ejbRefs.hasMoreElements();) {
            Element ejbRef = ejbRefs.next();
            String jndiName = ejbRef.getTextString("ejb-ref-name");
            String targetEjbName = ejbRef.getTextString("ejb-link");
            registerEjbRef(ejbName, jndiName, targetEjbName);
        }
    }
}
