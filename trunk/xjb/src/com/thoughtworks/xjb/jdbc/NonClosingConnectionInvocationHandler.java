/*
 * Created on 11-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;

import com.thoughtworks.proxytoys.DelegatingProxy;


class NonClosingConnectionInvocationHandler extends DelegatingProxy.DelegatingInvocationHandler {

    public NonClosingConnectionInvocationHandler(Connection conn) {
        super(conn);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("close".equals(method.getName())) {
            return null;
        }
        else {
            return super.invoke(proxy, method, args);
        }
    }
}