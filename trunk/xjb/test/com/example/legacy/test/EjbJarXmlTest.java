/*
 * Created on 24-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.example.legacy.test;

import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

import com.example.legacy.client.ExampleActionBean;
import com.thoughtworks.xjb.config.ejbjar.XjbEjbJarParser;

/**
 * This example demonstrates configuring XJB using an ejb-jar.xml.
 * 
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class EjbJarXmlTest extends TestCase {
    
    private static final String VALUE_FROM_ENVIRONMENT = "value from ENVIRONMENT";
    
	private Reader ejbJarXml() {
        return new StringReader((
            "<ejb-jar>\n" +
            "  <enterprise-beans>\n" +
            "    <session>\n" +
            "      <ejb-name>Example</ejb-name>\n" +
            "      <home>com.example.legacy.ejb.ExampleHome</home>\n" +
            "      <remote>com.example.legacy.ejb.Example</remote>\n" +
            "      <ejb-class>com.example.legacy.ejb.ExampleBean</ejb-class>\n" +
            "      <session-type>Stateless</session-type>\n" +
            "      <env-entry>\n" + 
            "        <env-entry-name>SOME_VALUE</env-entry-name>\n" + 
            "        <env-entry-type>java.lang.String</env-entry-type>\n" + 
            "        <env-entry-value>"+VALUE_FROM_ENVIRONMENT+"</env-entry-value>\n" + 
            "      </env-entry>\n" + 
            "    </session>" +
            "  </enterprise-beans>" +
            "</ejb-jar>"));
    }
    
    public void setUp() throws Exception {
        new XjbEjbJarParser().read(ejbJarXml()); // could use a FileReader
        // or: new XjbEjbJarParser().read("file:///path/to/some-ejb.jar!/META-INF/ejb-jar.xml");
    }
    
	public void testShouldCallEjbMethodFromClient() throws Exception {
        // setup
        ExampleActionBean bean = new ExampleActionBean();
        
        // execute
        String result = bean.getSomeValue();
        
        // verify
		assertEquals(VALUE_FROM_ENVIRONMENT, result);
    }
}
