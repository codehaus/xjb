/*
 * Created on 19-May-2004
 * 
 * (c) 2003-2004 ThoughtWorks Ltd
 *
 * See license.txt for license details
 */
package com.thoughtworks.xjb.cmt;

import java.lang.reflect.Method;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import com.thoughtworks.proxy.toys.decorate.Decorating;
import com.thoughtworks.proxy.toys.decorate.InvocationDecorator;
import com.thoughtworks.xjb.ejb.XjbRemoteFactory;
import com.thoughtworks.xjb.ejb.XjbSessionContext;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class CmtRemoteFactory extends XjbRemoteFactory {

    private static final Method isIdentical;
    static {
        try {
			isIdentical = EJBObject.class.getMethod("isIdentical", new Class[] {EJBObject.class});
		} catch (Exception e) {
			throw new NoSuchMethodError("EJBObject.isIdentical(EJBObject)");
		}
    }
    
	private class TransactionalDecorator implements InvocationDecorator {
        private boolean overrideResult = false;
        private Object result = null;
        
		public Object[] beforeMethodStarts(Object proxy, Method method, Object[] args) {
            if (isIdentical.equals(method)) {
                System.err.println(3);
                overrideResult = true;
                result = Boolean.valueOf(proxy == this);
            }
            else {
                overrideResult = false;
            }
            Policy policy = policyLookup.lookupPolicyFor(method);
            handler.onInvoke(policy);
            return args;
		}
        
		public Object decorateResult(Object before) {
            handler.onSuccess();
            return overrideResult ? result : before;
		}
        
		public Throwable decorateTargetException(Throwable cause) {
            handler.onFailure();
			return cause;
		}

		public Exception decorateInvocationException(Exception cause) {
            // TODO test for this?
			return null;
		}
	}
    
    private final PolicyLookup policyLookup;
    private final TransactionPolicyHandler handler;
    private final TransactionGetter transactionGetter;

    public CmtRemoteFactory(TransactionGetter transactionGetter, PolicyLookup policyLookup, TransactionPolicyHandler handler) {
        this.transactionGetter = transactionGetter;
        this.policyLookup = policyLookup;
        this.handler = handler;
    }
    
	public EJBObject createRemote(String ejbName, EJBHome ejbHome,
			Class remoteInterface, Object impl) {
        EJBObject inner = super.createRemote(ejbName, ejbHome, remoteInterface, impl);
        return (EJBObject)Decorating.object(remoteInterface, inner, new TransactionalDecorator());
	}
    
    
	protected XjbSessionContext createSessionContext(EJBHome ejbHome, EJBObject remote) {
		return new CmtSessionContext(ejbHome, remote, transactionGetter);
	}
}
