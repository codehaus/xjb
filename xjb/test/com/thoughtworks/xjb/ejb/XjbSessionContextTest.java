/*
 * Created on 04-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.ejb;

import javax.ejb.SessionContext;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbSessionContextTest extends TestCase {
    public void testShouldSetRollbackOnly() throws Exception {
        // setup
    	SessionContext ctx = new XjbSessionContext(null, null);
        assertFalse(ctx.getRollbackOnly());
        
        // execute
        ctx.setRollbackOnly();
        
        // verify
        assertTrue(ctx.getRollbackOnly());
    }
}
