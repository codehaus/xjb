/*
 * Created on 30-Mar-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import java.lang.reflect.Method;

import com.thoughtworks.proxy.toys.nullobject.Null;


/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public interface PolicyLookup {
    PolicyLookup NULL = (PolicyLookup) Null.object(PolicyLookup.class);

	Policy lookupPolicyFor(Method method);
}
