/*
 * Created on 27-Jul-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.log;

/**
 * Factory for very basic logger.
 * 
 * @see Log
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class LogFactory {

    public static Log getLog(Class type) {
        return new Log(type);
    }

}
