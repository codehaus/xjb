/*
 * Created on 10-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.ejb;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.thoughtworks.proxytoys.DelegatingProxy;
import com.thoughtworks.xjb.cmt.Policy;
import com.thoughtworks.xjb.cmt.PolicyLookup;
import com.thoughtworks.xjb.cmt.Transaction;
import com.thoughtworks.xjb.cmt.TransactionPolicyHandler;
import com.thoughtworks.xjb.cmt.TransactionalSessionContext;
import com.thoughtworks.xjb.jndi.XjbInitialContextFactory;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbRemoteFactory implements RemoteFactory {
    private static class RemoteInvocationHandler extends DelegatingProxy.DelegatingInvocationHandler {
        private final String ejbName;
        private final PolicyLookup policyLookup;
        private final TransactionPolicyHandler handler;
        private TransactionalSessionContext context;
        
        public RemoteInvocationHandler(String ejbName, Object impl, PolicyLookup policyLookup, TransactionPolicyHandler handler) {
            super(impl);
            this.ejbName = ejbName;
            this.policyLookup = policyLookup;
            this.handler = handler;
        }
        
		public void setSessionContext(TransactionalSessionContext context) {
			this.context = context;
			if (delegate instanceof SessionBean) {
				callSetSessionContext();
			}
		}
        
        private void callSetSessionContext() {
            try {
                invokeOnDelegate("setSessionContext", new Class[] {SessionContext.class}, new Object[] {context});
			} catch (RuntimeException e) {
                throw e;
			} catch (Error e) {
                throw e;
			} catch (Throwable e) {
                throw new RuntimeException("Unexpected exception: " + e + ": " + e.getMessage());
            }
        }
        
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            String oldContextName = XjbInitialContextFactory.getLocalContextName();
            try {
                Object result;
                
                Policy policy = policyLookup.lookupPolicyFor(method);
            	Transaction transaction = handler.beforeMethodStarts(policy);
                context.setTransaction(transaction);
                
	            XjbInitialContextFactory.setLocalContext(ejbName);
                if (isEjbMethod("remove", method)) {
                    result = invokeOnDelegate("ejbRemove", new Class[0], args);
                }
                else if (isEjbMethod("getEJBHome", method)) {
                    result = context.getEJBHome();
                }
                else if (isEjbMethod("isIdentical", method)) {
                    result = (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
                }
                else {
                    result = super.invoke(proxy, method, args);
                }
                handler.afterMethodEnds();
                return result;
            }
            catch (Exception e) {
                handler.afterMethodFails();
                throw e;
            }
            finally {
            	XjbInitialContextFactory.setLocalContext(oldContextName);
            }
        }
        
        private boolean isEjbMethod(String methodName, Method method) {
            return (method.getName().equals(methodName)
                    && method.getDeclaringClass().equals(EJBObject.class));
        }
    }
    
    public EJBObject createRemote(String ejbName, Class remoteInterface, EJBHome ejbHome, Object impl) {
        return createRemote(ejbName, remoteInterface, ejbHome, impl, PolicyLookup.NULL, TransactionPolicyHandler.NULL);
    }

    public EJBObject createRemote(String ejbName, Class remoteInterface, EJBHome ejbHome, Object impl, PolicyLookup policyLookup, TransactionPolicyHandler handler) {
        final RemoteInvocationHandler invocationHandler = new RemoteInvocationHandler(ejbName, impl, policyLookup, handler);
		final EJBObject result = (EJBObject) Proxy.newProxyInstance(
                remoteInterface.getClassLoader(),
                new Class[] {remoteInterface},
                invocationHandler);
        
        invocationHandler.setSessionContext(new XjbSessionContext(ejbHome, result));
        
        return result;
    }
}
