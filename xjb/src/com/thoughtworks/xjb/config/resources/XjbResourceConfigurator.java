/*
 * Created on 16-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.config.resources;

import java.io.Reader;
import java.rmi.RemoteException;

import javax.sql.DataSource;

import com.thoughtworks.xjb.jdbc.DataSourceFactory;
import com.thoughtworks.xjb.jndi.JndiRegistry;

import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.XPath;

/*
    <xjb>
        <data-sources>
            <data-source>
                <jndi-name>jdbc/dataSource</jndi-name>
                <driver-class>some.JdbcDriver</driver-class>
                <jdbc-url>jdbc:some:url</jdbc-url>
                <user>dbo</user>
                <password>secret</password>
            </data-source>
        </data-sources>
    </xjb>

 */

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbResourceConfigurator implements ResourceConfigurator {

    private final DataSourceFactory dataSourceFactory;
	private final JndiRegistry jndiRegistry;

	public XjbResourceConfigurator(JndiRegistry jndiRegistry, DataSourceFactory dataSourceFactory) {
		this.dataSourceFactory = dataSourceFactory;
		this.jndiRegistry = jndiRegistry;
	}

	public void read(Reader xmlConfig) throws RemoteException {
        try {
            Elements dataSources = new Document(xmlConfig).getElements(new XPath("xjb/data-sources/data-source"));
            while (dataSources.hasMoreElements()) {
            	Element element = dataSources.next();
	            String jndiName = element.getTextString("jndi-name");
                String driverClass = element.getTextString("driver-class");
	            String jdbcUrl = element.getTextString("jdbc-url");
	            String user = element.getTextString("user");
	            String password = element.getTextString("password");

                ensureDriveClassrIsLoaded(driverClass);
	            DataSource dataSource = dataSourceFactory.createDriverManagerDataSource(jdbcUrl, user, password);
				jndiRegistry.register(jndiName, dataSource);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("", e);
        }
    }

	private void ensureDriveClassrIsLoaded(String driverClass) throws ClassNotFoundException {
		Class.forName(driverClass);
	}
}
