/*
 * Created on 01-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbTransaction implements Transaction {
    private Set connections = new HashSet();
	private boolean rollbackOnly = false;
    
	public void commitUnlessRollbackOnly() {
        if (rollbackOnly) {
            rollback();
            return;
        }
        for (Iterator i = connections.iterator(); i.hasNext();) {
    		try {
                Connection conn = (Connection) i.next();
    			conn.commit();
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
        connections.clear();
	}

    public void rollback() {
        for (Iterator i = connections.iterator(); i.hasNext();) {
            try {
                Connection conn = (Connection) i.next();
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        connections.clear();
	}
	
    public void registerConnection(Connection connection) {
        connections.add(connection);
	}
    
    public Collection getConnections() {
        return Collections.unmodifiableCollection(connections);
    }

	public void setRollbackOnly() {
        rollbackOnly = true;
	}
}
