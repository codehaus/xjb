/*
 * Created on 24-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.ejb;

import java.io.Serializable;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
class XjbEJBMetaData implements EJBMetaData, Serializable {
    private final EJBHome ejbHome;
    private final Class homeInterface;
    private final Class remoteInterface;
    private final boolean stateless;

    public XjbEJBMetaData(EJBHome ejbHome, Class homeInterface, Class remoteInterface, boolean stateless) {
        this.ejbHome = ejbHome;
        this.homeInterface = homeInterface;
        this.remoteInterface = remoteInterface;
        this.stateless = stateless;
    }

    public EJBHome getEJBHome() {
        return ejbHome;
    }

    public Class getHomeInterfaceClass() {
        return homeInterface;
    }

    public Class getRemoteInterfaceClass() {
        return remoteInterface;
    }

    public Class getPrimaryKeyClass() {
        return null;
    }

    public boolean isSession() {
        return true;
    }

    public boolean isStatelessSession() {
        return stateless;
    }
}
