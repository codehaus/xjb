/*
 * Created on 06-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.config.ejbjar.xpp;

import java.io.Reader;
import java.rmi.RemoteException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;

import com.thoughtworks.xjb.config.ejbjar.EjbJarConfiguratorSupport;
import com.thoughtworks.xjb.jndi.JndiRegistry;
import com.thoughtworks.xjb.jndi.XjbInitialContextFactory;

/**
 * Reads and configures the test framework from one or more <tt>ejb-jar.xml</tt> files.
 * 
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XppEjbJarConfigurator extends EjbJarConfiguratorSupport {
    private static final Logger log = Logger.getLogger(XppEjbJarConfigurator.class);
    
    public final XmlPullParser xpp = new MXParser();
    
    public XppEjbJarConfigurator(JndiRegistry jndiRegistry, Context context) throws NamingException {
        super(jndiRegistry, context);
    }

    public XppEjbJarConfigurator() throws NamingException {
		this(new XjbInitialContextFactory(), new InitialContext());
	}

	public void read(Reader in) throws RemoteException {
        try {
            xpp.setInput(in);
            for (int xppEvent = xpp.next(); xppEvent != XmlPullParser.END_DOCUMENT; xppEvent = xpp.next()) {
                if (xppEvent == XmlPullParser.START_TAG && "enterprise-beans".equals(xpp.getName())) {
                    parseEnterpriseBeans();
                }
            }
        }
        catch (Exception e) {
            throw new RemoteException("Unable to parse xml", e);
        }
    }

    private void parseEnterpriseBeans() throws Exception {
        for (int xppEvent = xpp.next(); stillInElement("enterprise-beans"); xppEvent = xpp.next()) {
            if (xppEvent == XmlPullParser.START_TAG && "session".equals(xpp.getName())) {
                parseSession();
            }
        }
    }

    private void parseSession() throws Exception {
        String ejbName = null;
        Class homeInterface = null;
        Class remoteInterface = null;
        Class ejbClass = null;
        boolean isStateless = true;

        for (int xppEvent = xpp.next(); stillInElement("session"); xppEvent = xpp.next()) {
            if (xppEvent == XmlPullParser.START_TAG) {
                String tag = xpp.getName();

                // data fields
                if ("ejb-name".equals(tag)) {
                    ejbName = getTextString();
                }
                else if ("home".equals(tag)) {
                    homeInterface = Class.forName(getTextString());
                }
                else if ("remote".equals(tag)) {
                    remoteInterface = Class.forName(getTextString());
                }
                else if ("ejb-class".equals(tag)) {
                    ejbClass = Class.forName(getTextString());
                }
                else if ("session-type".equals(tag)) {
                    isStateless = "Stateless".equals(getTextString());
                }

                // sub-elements
                else if ("env-entry".equals(tag)) {
                    parseEnvEntry(ejbName);
                }
                else if ("resource-ref".equals(tag)) {
                    parseResourceRef(ejbName);
                }
                else if ("ejb-ref".equals(tag)) {
                    log.debug("Parsing ejb-ref for " + ejbName);
                    parseEjbRef(ejbName);
                }
            }
        }
        registerSessionBean(ejbName, homeInterface, remoteInterface, ejbClass, isStateless);
    }

    private void parseEnvEntry(String ejbName) throws Exception {
        String entryName = null;
        String entryValue = null;
        Class entryType = null;

        for (int xppEvent = xpp.next(); stillInElement("env-entry"); xppEvent = xpp.next()) {
            if (xppEvent == XmlPullParser.START_TAG) {
                String tag = xpp.getName();
                if ("env-entry-name".equals(tag)) {
                    entryName = getTextString();
                }
                else if ("env-entry-type".equals(tag)) {
                    entryType = Class.forName(getTextString());
                }
                else if ("env-entry-value".equals(tag)) {
                    entryValue = getTextString();
                }
            }
        }
        registerEnvEntry(ejbName, entryName, entryType, entryValue);
    }

    private void parseResourceRef(String ejbName) throws Exception {
        Class resType = null;
        String refName = null;

        for (int xppEvent = xpp.next(); stillInElement("resource-ref"); xppEvent = xpp.next()) {
            if (xppEvent == XmlPullParser.START_TAG) {
                String tag = xpp.getName();
                if ("res-type".equals(tag)) {
                    resType = Class.forName(getTextString());
                }
                else if ("res-ref-name".equals(tag)) {
                    refName = getTextString();
                }
            }
        }
        registerResourceRef(ejbName, refName, resType);
    }
    
    private void parseEjbRef(String ejbName) throws Exception {
        String jndiName = null;
        String targetEjbName = null;
        
        for (int xppEvent = xpp.next(); stillInElement("ejb-ref"); xppEvent = xpp.next()) {
            if (xppEvent == XmlPullParser.START_TAG) {
                String tag = xpp.getName();
                if ("ejb-ref-name".equals(tag)) {
                    jndiName = getTextString();
                }
                else if ("ejb-link".equals(tag)) {
                    targetEjbName = getTextString();
                }
            }
        }
        registerEjbRef(ejbName, jndiName, targetEjbName);
    }

    private boolean stillInElement(String element) throws Exception {
        return !(xpp.getEventType() == XmlPullParser.END_TAG && element.equals(xpp.getName()));
    }

    private String getTextString() throws Exception {
        while (xpp.next() != XmlPullParser.TEXT)
            ;
        return xpp.getText();
    }
}
