/*
 * Created on 21-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.util;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class Logger {

	public static Logger getLogger(Class clazz) {
		return new Logger();
	}

	public void debug(String msg) {
		// TODO debugging
	}
}
