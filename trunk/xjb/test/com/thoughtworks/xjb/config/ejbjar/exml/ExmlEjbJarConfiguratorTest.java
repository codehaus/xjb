/*
 * Created on 01-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.config.ejbjar.exml;

import javax.naming.Context;

import com.thoughtworks.xjb.config.ejbjar.EjbJarConfigurator;
import com.thoughtworks.xjb.config.ejbjar.EjbJarConfiguratorTestCase;
import com.thoughtworks.xjb.jndi.JndiRegistry;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class ExmlEjbJarConfiguratorTest extends EjbJarConfiguratorTestCase {

	protected EjbJarConfigurator createConfigurator(JndiRegistry registry, Context context) throws Exception {
		return new ExmlEjbJarConfigurator(registry, context);
	}
}
