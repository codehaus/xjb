/*
 * Created on 04-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.thoughtworks.xjb.cmt.DataSourceTransactionHandlingTest;
import com.thoughtworks.xjb.cmt.HomeFactoryTransactionHandlingTest;
import com.thoughtworks.xjb.cmt.RemoteFactoryTransactionHandlingTest;
import com.thoughtworks.xjb.cmt.SessionContextTransactionHandlingTest;
import com.thoughtworks.xjb.cmt.XjbTransactionHandlerTest;
import com.thoughtworks.xjb.cmt.XjbTransactionTest;
import com.thoughtworks.xjb.config.ejbjar.EjbJarConfiguratorTest;
import com.thoughtworks.xjb.config.resources.ResourceConfiguratorTest;
import com.thoughtworks.xjb.ejb.HomeFactoryTest;
import com.thoughtworks.xjb.ejb.RemoteFactoryTest;
import com.thoughtworks.xjb.ejb.XjbSessionContextTest;
import com.thoughtworks.xjb.jdbc.JdbcDataSourceFactoryTest;
import com.thoughtworks.xjb.jdbc.NullDriverTest;
import com.thoughtworks.xjb.jndi.XjbInitialContextFactoryBuilderTest;
import com.thoughtworks.xjb.jndi.XjbInitialContextFactoryTest;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class AllTests {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AllTests.class);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.thoughtworks.xjb");
        //$JUnit-BEGIN$
        suite.addTestSuite(DataSourceTransactionHandlingTest.class);
        suite.addTestSuite(HomeFactoryTransactionHandlingTest.class);
        suite.addTestSuite(RemoteFactoryTransactionHandlingTest.class);
        suite.addTestSuite(XjbTransactionHandlerTest.class);
        suite.addTestSuite(SessionContextTransactionHandlingTest.class);
        suite.addTestSuite(XjbTransactionTest.class);
        suite.addTestSuite(EjbJarConfiguratorTest.class);
        suite.addTestSuite(ResourceConfiguratorTest.class);
        suite.addTestSuite(JdbcDataSourceFactoryTest.class);
        suite.addTestSuite(NullDriverTest.class);
        suite.addTestSuite(HomeFactoryTest.class);
        suite.addTestSuite(RemoteFactoryTest.class);
        suite.addTestSuite(XjbInitialContextFactoryBuilderTest.class);
        suite.addTestSuite(XjbInitialContextFactoryTest.class);
        suite.addTestSuite(XjbSessionContextTest.class);
        
        suite.addTest(com.example.legacy.test.AllTests.suite());
        //$JUnit-END$
        return suite;
    }
}
