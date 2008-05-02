/*
 * Created on 24-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.config.resources;

import java.io.Reader;
import java.io.StringReader;

import javax.sql.DataSource;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import com.thoughtworks.proxy.toys.nullobject.Null;
import com.thoughtworks.xjb.config.MapRegistry;
import com.thoughtworks.xjb.jdbc.DataSourceFactory;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class ResourceConfiguratorTest extends MockObjectTestCase {
    private static final String createDriverManagerDataSource = "createDriverManagerDataSource";

    public static class FirstTestDriver {
    }

    public static class SecondTestDriver {
    }

    private Reader dataSourceXml() {
        String xml =
            "<xjb>\n" + 
            "   <data-sources>\n" + 
            "       <data-source>\n" + 
            "           <jndi-name>jdbc/One</jndi-name>\n" + 
            "           <driver-class>" + FirstTestDriver.class.getName() + "</driver-class>\n" + 
            "           <jdbc-url>jdbc:test:one</jdbc-url>\n" + 
            "           <user>first</user>\n" + 
            "           <password>first-secret</password>\n" + 
            "       </data-source>\n" + 
            "       <data-source>\n" + 
            "           <jndi-name>jdbc/Two</jndi-name>\n" + 
            "           <driver-class>" + SecondTestDriver.class.getName() + "</driver-class>\n" + 
            "           <jdbc-url>jdbc:test:two</jdbc-url>\n" + 
            "           <user>second</user>\n" + 
            "           <password>second-secret</password>\n" + 
            "       </data-source>\n" + 
            "   </data-sources>\n" + 
            "</xjb>";
        return new StringReader(xml);
    }
    
    public void testShouldConfigureDataSources() throws Exception {
		// setup
    	Mock mockFactory = new Mock(DataSourceFactory.class);
    	
        mockFactory.expects(once()).method(createDriverManagerDataSource)
            .with(eq("jdbc:test:one"), eq("first"), eq("first-secret"))
		    .will(returnValue(Null.object(DataSource.class)));
        
    	mockFactory.expects(once()).method(createDriverManagerDataSource)
            .with(eq("jdbc:test:two"), eq("second"), eq("second-secret"))
		    .will(returnValue(Null.object(DataSource.class)));
        
        DataSourceFactory dataSourceFactory = (DataSourceFactory) mockFactory.proxy();
        
        MapRegistry registry = new MapRegistry();
        
        ResourceConfigurator configurator = new XjbResourceConfigurator(registry, dataSourceFactory);
        
        // execute
        configurator.read(dataSourceXml());
        
        // verify
        DataSource one = (DataSource) registry.get("jdbc/One");
        assertNotNull("jdbc/One", one);
		assertTrue("jdbc/One should be a null object", Null.isNullObject(one));

        DataSource two = (DataSource) registry.get("jdbc/Two");
        assertNotNull("jdbc/Two", two);
		assertTrue("jdbc/Two should be a null object", Null.isNullObject(two));

        mockFactory.verify();
    }
}
