/*
 * Created on 11-Apr-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;


class SingleConnectionInvocationHandler implements InvocationHandler {
    private final Connection conn;
    private final Connection nonClosingConn;
    
    public SingleConnectionInvocationHandler(Connection conn) {
        this.conn = conn;
        this.nonClosingConn = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class[] {Connection.class},
                new NonClosingConnectionInvocationHandler(conn));
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getConnection".equals(method.getName())) {
            return nonClosingConn;
        }
        else if ("toString".equals(method.getName())) {
            return "XJB single connection DataSource: conn = " + conn;
        }
        else {
            throw new UnsupportedOperationException(method.getName());
        }
    }
}