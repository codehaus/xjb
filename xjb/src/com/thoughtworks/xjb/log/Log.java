/*
 * Created on 27-Jul-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.log;

/**
 * Really <i>very</i> basic logger.
 * 
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class Log {
    private static final boolean DEBUG = Boolean.getBoolean(System.getProperty("debug", "false"));
    private final Class type;

    public Log(Class type) {
        this.type = type;
    }

    public void debug(String message) {
        if (DEBUG) {
            System.out.println(type.getName() + ": " + message);
        }
    }

}
