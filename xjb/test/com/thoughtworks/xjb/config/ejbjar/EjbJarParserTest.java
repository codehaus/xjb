/*
 * Created on 06-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.config.ejbjar;

import java.io.Reader;
import java.io.StringReader;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.Context;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.jmock.Mock;
import org.jmock.core.mixin.Invoked;
import org.jmock.core.mixin.Is;
import org.jmock.core.mixin.Return;
import org.jmock.core.mixin.Throw;

import com.thoughtworks.xjb.config.MapRegistry;
import com.thoughtworks.xjb.ejb.SessionBeanSupport;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class EjbJarParserTest extends TestCase {

    private MapRegistry mapRegistry;
    private Mock contextMock;
    private EjbJarParser parser;

	private EjbJarParser createParser() throws Exception {
		return new XjbEjbJarParser(mapRegistry, null);
	}
    
    public void setUp() throws Exception {
        mapRegistry = new MapRegistry();
        contextMock = new Mock(Context.class);
        parser = new XjbEjbJarParser(mapRegistry, (Context) contextMock.proxy());
    }
    
    public interface Simple extends EJBObject {
        String getSomething() throws Exception;
    }
    
    public interface SimpleHome extends EJBHome {
        Simple create() throws Exception;
    }
    
    public static class SimpleBean extends SessionBeanSupport {
        public String getSomething() throws Exception {
            return "something";
        }
    }
    
    private Reader simpleBeanXml(String home, String remote, String ejbClass, boolean isStateless) {
        String stateless = isStateless ? "Stateless" : "Stateful";
        
        final StringBuffer xml = new StringBuffer();
        xml.append("<ejb-jar>\n");
        xml.append("  <enterprise-beans>\n");
        xml.append("    <session>\n");
        xml.append("      <ejb-name>Simple</ejb-name>\n");
        xml.append("      <home>" + home + "</home>\n");
        xml.append("      <remote>" + remote + "</remote>\n");
        xml.append("      <ejb-class>" + ejbClass + "</ejb-class>\n");
        xml.append("      <session-type>" + stateless + "</session-type>\n");
        xml.append("    </session>\n");
        xml.append("  </enterprise-beans>\n");
        xml.append("</ejb-jar>\n");
        return new StringReader(xml.toString());
    }
    
    private Reader statelessXml() {
        return simpleBeanXml(
                SimpleHome.class.getName(),
                Simple.class.getName(),
                SimpleBean.class.getName(),
                true);        
    }
    
    public void testShouldConfigureStatelessSessionBean() throws Exception {
        // execute
		parser.read(statelessXml());
        SimpleHome home = (SimpleHome) mapRegistry.get("Simple");
        String result = home.create().getSomething();
		
        // verify
        assertEquals("something", result);
        assertTrue("should be stateless", home.getEJBMetaData().isStatelessSession());
    }
    
    private void assertInstanceOf(Class type, Object object) throws Exception {
    	assertTrue("Should be a " + type.getName(), type.isAssignableFrom(object.getClass()));
	}
    
    public void testShouldMapBeanToCommonJndiNames() throws Exception {
        // execute
		parser.read(statelessXml());
		
        // verify
		assertInstanceOf(SimpleHome.class, mapRegistry.get("Simple"));
		assertInstanceOf(SimpleHome.class, mapRegistry.get("ejb/Simple"));
		assertInstanceOf(SimpleHome.class, mapRegistry.get("com/thoughtworks/xjb/config/ejbjar/EjbJarParserTest$Simple"));
    }

	private Reader statefulXml() {
        return simpleBeanXml(
                SimpleHome.class.getName(),
                Simple.class.getName(),
                SimpleBean.class.getName(),
                false);        
    }

    public void testShouldConfigureStatefulSessionBean() throws Exception {
        // execute
		parser.read(statefulXml());
        SimpleHome home = (SimpleHome) mapRegistry.get("Simple");
        String result = home.create().getSomething();
        
        // verify
        assertEquals("something", result);
        assertFalse("should not be stateless", home.getEJBMetaData().isStatelessSession());
    }
    
    private void assertCannotResolveClass(String testName, String home, String remote, String ejbClass) throws Exception {
        try {
			parser.read(simpleBeanXml(home, remote, ejbClass, true));
            fail("Missing " + testName + " should have thrown RemoteException");
        } catch (RemoteException e) {
            // expected
        }
    }

    public void testShouldThrowRemoteExceptionIfHomeInterfaceCannotBeResolved() throws Exception {
        assertCannotResolveClass (
                "home",
                "non.existent.HomeInterface",
                Simple.class.getName(),
                SimpleBean.class.getName());
    }

    public void testShouldThrowRemoteExceptionIfRemoteInterfaceCannotBeResolved() throws Exception {
        assertCannotResolveClass(
                "remote",
                SimpleHome.class.getName(),
                "non.existent.RemoteInterface",
                SimpleBean.class.getName());
    }

    public void testShouldThrowRemoteExceptionIfEjbClassCannotBeResolved() throws Exception {
        assertCannotResolveClass(
                "ejb class",
                SimpleHome.class.getName(),
                Simple.class.getName(),
                "non.existent.EjbClass");
    }
    
    private Reader beanWithEnvEntriesXml() {
        String remote = Simple.class.getName();
        String home = SimpleHome.class.getName();
        String ejbClass = SimpleBean.class.getName();

        final StringBuffer xml = new StringBuffer();
        xml.append("<ejb-jar id='ejb-jar_ID'>\n");
        xml.append("    <enterprise-beans>\n");
        xml.append("        <session>\n");
        xml.append("            <ejb-name>EnvEntry</ejb-name>\n");
        xml.append("            <home>" + home + "</home>\n");
        xml.append("            <remote>" + remote + "</remote>\n");
        xml.append("            <ejb-class>"+ ejbClass + "</ejb-class>\n");
        xml.append("            <session-type>Stateless</session-type>\n");
        xml.append("            <env-entry>\n");
        xml.append("                <env-entry-name>STRING</env-entry-name>\n");
        xml.append("                <env-entry-type>java.lang.String</env-entry-type>\n");
        xml.append("                <env-entry-value>string value</env-entry-value>\n");
        xml.append("            </env-entry>\n");
        xml.append("            <env-entry>\n");
        xml.append("                <env-entry-name>INTEGER</env-entry-name>\n");
        xml.append("                <env-entry-type>java.lang.Integer</env-entry-type>\n");
        xml.append("                <env-entry-value>1</env-entry-value>\n");
        xml.append("            </env-entry>\n");
        xml.append("        </session>\n");
        xml.append("    </enterprise-beans>\n");
        xml.append("</ejb-jar>\n");
        return new StringReader(xml.toString());
    }
        
    public void testShouldConfigureEnvEntries() throws Exception {
        // execute
        createParser().read(beanWithEnvEntriesXml());
        
        // verify
        assertEquals("string value", mapRegistry.get("EnvEntry|STRING"));
        assertEquals(new Integer(1), mapRegistry.get("EnvEntry|INTEGER"));
    }
    
    public static class AnotherBean extends SessionBeanSupport {
        public String getSomething() throws Exception {
            return "something else";
        }
    }
    
    private Reader twoBeansXml() {
        String remote = Simple.class.getName();
        String home = SimpleHome.class.getName();
        String simpleEjbClass = SimpleBean.class.getName();
        String anotherBeanClass = AnotherBean.class.getName();
        
        final StringBuffer xml = new StringBuffer();
        xml.append("<ejb-jar>\n");
        xml.append("    <enterprise-beans>\n");
        xml.append("        <session>\n");
        xml.append("            <ejb-name>Simple</ejb-name>\n");
        xml.append("            <home>" + home + "</home>\n");
        xml.append("            <remote>" + remote + "</remote>\n");
        xml.append("            <ejb-class>"+ simpleEjbClass + "</ejb-class>\n");
        xml.append("            <session-type>Stateless</session-type>\n");
        xml.append("        </session>\n");
        xml.append("        <session>\n");
        xml.append("            <ejb-name>Another</ejb-name>\n");
        xml.append("            <home>" + home + "</home>\n");
        xml.append("            <remote>" + remote + "</remote>\n");
        xml.append("            <ejb-class>"+ anotherBeanClass + "</ejb-class>\n");
        xml.append("            <session-type>Stateless</session-type>\n");
        xml.append("        </session>\n");
        xml.append("  </enterprise-beans>\n");
        xml.append("</ejb-jar>\n");
        return new StringReader(xml.toString());
    }
    
    public void testShouldConfigureMultipleSessionBeans() throws Exception {
        // execute
        createParser().read(twoBeansXml());
        SimpleHome simpleHome = (SimpleHome) mapRegistry.get("Simple");
        SimpleHome anotherHome = (SimpleHome) mapRegistry.get("Another");

        assertEquals("something", simpleHome.create().getSomething());
        assertEquals("something else", anotherHome.create().getSomething());
    }
    
    private Reader beanWithResourceRefXml() {
        String remote = Simple.class.getName();
        String home = SimpleHome.class.getName();
        String ejbClass = SimpleBean.class.getName();

        final StringBuffer xml = new StringBuffer();
        xml.append("<ejb-jar id='ejb-jar_ID'>\n");
        xml.append("    <enterprise-beans>\n");
        xml.append("        <session>\n");
        xml.append("            <ejb-name>ResourceRefBean</ejb-name>\n");
        xml.append("            <home>" + home + "</home>\n");
        xml.append("            <remote>" + remote + "</remote>\n");
        xml.append("            <ejb-class>"+ ejbClass + "</ejb-class>\n");
        xml.append("            <session-type>Stateless</session-type>\n");
        xml.append("            <resource-ref>\n");
        xml.append("                <res-ref-name>some/Resource</res-ref-name>\n");
        xml.append("                <res-type>java.lang.String</res-type>\n");
        xml.append("                <res-auth>Container</res-auth>\n");
        xml.append("                <res-sharing-scope>Shareable</res-sharing-scope>\n");
        xml.append("            </resource-ref>\n");
        xml.append("            <resource-ref>\n");
        xml.append("                <res-ref-name>some/OtherResource</res-ref-name>\n");
        xml.append("                <res-type>java.lang.String</res-type>\n");
        xml.append("                <res-auth>Container</res-auth>\n");
        xml.append("                <res-sharing-scope>Shareable</res-sharing-scope>\n");
        xml.append("            </resource-ref>\n");
        xml.append("        </session>\n");
        xml.append("    </enterprise-beans>\n");
        xml.append("</ejb-jar>\n");
        return new StringReader(xml.toString());
    }
    
    public void testShouldResolveResourceReferencesIntoLocalContext() throws Exception {
    	// setup
    	contextMock.expects(Invoked.once()).method("lookup")
			.with(Is.equal("some/Resource"))
			.will(Return.value("first resource"));
        
    	contextMock.expects(Invoked.once()).method("lookup")
			.with(Is.equal("some/OtherResource"))
			.will(Return.value("second resource"));

    	// execute
		parser.read(beanWithResourceRefXml());
		
		// verify
		assertEquals("first resource", mapRegistry.get("ResourceRefBean|some/Resource"));
		assertEquals("second resource", mapRegistry.get("ResourceRefBean|some/OtherResource"));
		contextMock.verify();
    }
    
    public void testShouldThrowRemoteExceptionIfUnableToResolveResourceReference() throws Exception {
    	// setup
    	contextMock.expects(Invoked.once()).method("lookup")
			.with(Is.equal("some/Resource"))
			.will(Throw.exception(new NamingException()));

        // execute
        try {
			parser.read(beanWithResourceRefXml());
            fail("should have thrown RemoteException");
        } catch (RemoteException e) {
            // verify
        	contextMock.verify();
        }
    }
    
    private Reader beanWithEjbRefXml() {
        String remote = Simple.class.getName();
        String home = SimpleHome.class.getName();
        String ejbClass = SimpleBean.class.getName();
        
        final StringBuffer xml = new StringBuffer();
        xml.append("<ejb-jar id='ejb-jar_ID'>\n");
        xml.append("    <enterprise-beans>\n");
        xml.append("        <session>\n");
        xml.append("            <ejb-name>BeanWithEjbRef</ejb-name>\n");
        xml.append("            <home>" + home + "</home>\n");
        xml.append("            <remote>" + remote + "</remote>\n");
        xml.append("            <ejb-class>"+ ejbClass + "</ejb-class>\n");
        xml.append("            <session-type>Stateless</session-type>\n");
        xml.append("            <ejb-ref>\n");
        xml.append("                <ejb-ref-name>ejb/One</ejb-ref-name>\n");
        xml.append("                <home>" + home + "</home>\n");
        xml.append("                <remote>" + remote + "</remote>\n");
        xml.append("                <ejb-ref-type>Session</ejb-ref-type>\n");
        xml.append("                <ejb-link>Simple</ejb-link>\n");
        xml.append("            </ejb-ref>\n");
        xml.append("            <ejb-ref>\n");
        xml.append("                <ejb-ref-name>ejb/Two</ejb-ref-name>\n");
        xml.append("                <home>" + home + "</home>\n");
        xml.append("                <remote>" + remote + "</remote>\n");
        xml.append("                <ejb-ref-type>Session</ejb-ref-type>\n");
        xml.append("                <ejb-link>Simple</ejb-link>\n");
        xml.append("            </ejb-ref>\n");
        xml.append("        </session>\n");
        xml.append("    </enterprise-beans>\n");
        xml.append("</ejb-jar>\n");
        return new StringReader(xml.toString());
    }
    
    public void testShouldResolveEjbLinkReferencesIntoLocalContext() throws Exception {
        
    	// execute
    	parser.read(statelessXml());
        parser.read(beanWithEjbRefXml());
    	
    	// verify
        SimpleHome home = (SimpleHome) mapRegistry.get("BeanWithEjbRef|ejb/One");
        assertEquals("something", home.create().getSomething());
        
        SimpleHome home2 = (SimpleHome) mapRegistry.get("BeanWithEjbRef|ejb/Two");
        assertSame(home, home2);
    }
    
    public static class FirstCircularBean extends SimpleBean {
            public String getSomething() throws Exception {
            return "first";
        }
    }
    
    public static class SecondCircularBean extends SimpleBean {
            public String getSomething() throws Exception {
            return "second";
        }
    }
    
    private Reader circularEjbLinksPartOneXml() {
        String remote = Simple.class.getName();
        String home = SimpleHome.class.getName();
        String ejbClass = FirstCircularBean.class.getName();

        final StringBuffer xml = new StringBuffer();
        xml.append("<ejb-jar id='ejb-jar_ID'>\n");
        xml.append("    <enterprise-beans>\n");
        xml.append("        <session>\n");
        xml.append("            <ejb-name>FirstBean</ejb-name>\n");
        xml.append("            <home>" + home + "</home>\n");
        xml.append("            <remote>" + remote + "</remote>\n");
        xml.append("            <ejb-class>"+ ejbClass + "</ejb-class>\n");
        xml.append("            <session-type>Stateless</session-type>\n");
        xml.append("            <ejb-ref>\n");
        xml.append("                <ejb-ref-name>ejb/Other</ejb-ref-name>\n");
        xml.append("                <home>" + home + "</home>\n");
        xml.append("                <remote>" + remote + "</remote>\n");
        xml.append("                <ejb-ref-type>Session</ejb-ref-type>\n");
        xml.append("                <ejb-link>SecondBean</ejb-link>\n");
        xml.append("            </ejb-ref>\n");
        xml.append("        </session>\n");
        xml.append("    </enterprise-beans>\n");
        xml.append("</ejb-jar>\n");
        return new StringReader(xml.toString());
    }
    
    private Reader circularEjbLinksPartTwoXml() {
        String remote = Simple.class.getName();
        String home = SimpleHome.class.getName();
        String ejbClass = SecondCircularBean.class.getName();

        final StringBuffer xml = new StringBuffer();
        xml.append("<ejb-jar id='ejb-jar_ID'>\n");
        xml.append("    <enterprise-beans>\n");
        xml.append("        <session>\n");
        xml.append("            <ejb-name>SecondBean</ejb-name>\n");
        xml.append("            <home>" + home + "</home>\n");
        xml.append("            <remote>" + remote + "</remote>\n");
        xml.append("            <ejb-class>"+ ejbClass + "</ejb-class>\n");
        xml.append("            <session-type>Stateless</session-type>\n");
        xml.append("            <ejb-ref>\n");
        xml.append("                <ejb-ref-name>ejb/Other</ejb-ref-name>\n");
        xml.append("                <home>" + home + "</home>\n");
        xml.append("                <remote>" + remote + "</remote>\n");
        xml.append("                <ejb-ref-type>Session</ejb-ref-type>\n");
        xml.append("                <ejb-link>FirstBean</ejb-link>\n");
        xml.append("            </ejb-ref>\n");
        xml.append("        </session>\n");
        xml.append("    </enterprise-beans>\n");
        xml.append("</ejb-jar>\n");
        return new StringReader(xml.toString());
    }
    
	public void testShouldResolveCircularEjbLinksFromDifferentEjbJarXmlFiles() throws Exception {

		// execute
		parser.read(circularEjbLinksPartOneXml());
		parser.read(circularEjbLinksPartTwoXml());
		
		// verify
		SimpleHome secondHome = (SimpleHome)mapRegistry.get("FirstBean|ejb/Other");
		assertEquals("second", secondHome.create().getSomething());
		
		SimpleHome firstHome = (SimpleHome)mapRegistry.get("SecondBean|ejb/Other");
		assertEquals("first", firstHome.create().getSomething());
	}
}
