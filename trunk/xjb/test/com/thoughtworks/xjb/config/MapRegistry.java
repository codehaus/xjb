/*
 * Created on 21-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.config;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xjb.jndi.JndiRegistry;


public class MapRegistry implements JndiRegistry {
	public final Map map = new HashMap();
	
	public void register(String jndiName, Object object) {
		map.put(jndiName, object);
	}

	public void register(String contextName, String jndiName, Object object) {
		map.put(contextName + '|' + jndiName, object);
	}
    
    public Object get(Object key) {
        return map.get(key);
    }
}