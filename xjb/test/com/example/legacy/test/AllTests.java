/*
 * Created on 24-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.example.legacy.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class AllTests {
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.example.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(HardCodedTest.class);
        suite.addTestSuite(EjbJarXmlTest.class);
        //$JUnit-END$
        return suite;
    }
}
