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

import junit.framework.TestCase;

import org.jmock.Mock;
import org.jmock.core.mixin.Invoked;
import org.jmock.core.mixin.Is;
import org.jmock.core.mixin.Return;

import com.thoughtworks.proxy.toys.nullobject.Null;
import com.thoughtworks.xjb.config.MapRegistry;
import com.thoughtworks.xjb.jdbc.DataSourceFactory;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class ResourceConfiguratorTest extends TestCase {
    private static final String createDriverManagerDataSource = "createDriverManagerDataSource";
	private static boolean firstTestDriverClassLoaded;
    private static boolean secondTestDriverClassLoaded;

    public static class FirstTestDriver {
        static {
            firstTestDriverClassLoaded = true;
        }
    }

    public static class SecondTestDriver {
        static {
            secondTestDriverClassLoaded = true;
        }
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
        firstTestDriverClassLoaded = false;
        secondTestDriverClassLoaded = false;
        
    	Mock mockFactory = new Mock(DataSourceFactory.class);
    	
        mockFactory.expects(Invoked.once()).method(createDriverManagerDataSource)
            .with(Is.equal("jdbc:test:one"), Is.equal("first"), Is.equal("first-secret"))
		    .will(Return.value(Null.object(DataSource.class)));
        
    	mockFactory.expects(Invoked.once()).method(createDriverManagerDataSource)
            .with(Is.equal("jdbc:test:two"), Is.equal("second"), Is.equal("second-secret"))
		    .will(Return.value(Null.object(DataSource.class)));
        
        DataSourceFactory dataSourceFactory = (DataSourceFactory) mockFactory.proxy();
        
        MapRegistry registry = new MapRegistry();
        
        ResourceConfigurator configurator = new XjbResourceConfigurator(registry, dataSourceFactory);
        
        // execute
        configurator.read(dataSourceXml());
        
        // verify
        assertEquals(true, firstTestDriverClassLoaded);
        DataSource one = (DataSource) registry.get("jdbc/One");
        assertNotNull("jdbc/One should not be null", one);
		assertTrue("jdbc/One should be a null object", Null.isNullObject(one));

        assertEquals(true, secondTestDriverClassLoaded);
        DataSource two = (DataSource) registry.get("jdbc/Two");
        assertNotNull("jdbc/Two should not be null", two);
		assertTrue("jdbc/Two should be a null object", Null.isNullObject(two));

        mockFactory.verify();
    }
}
