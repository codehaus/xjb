/*
 * Created on 10-Feb-2004
 * 
 * (c) 2003-2004 ThoughtWorks
 * 
 * See license.txt for licence details
 */
package com.thoughtworks.xjb.ejb;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.SessionBean;

import com.thoughtworks.proxy.Invoker;
import com.thoughtworks.proxy.factory.StandardProxyFactory;
import com.thoughtworks.proxy.toys.decorate.DecoratingInvoker;
import com.thoughtworks.proxy.toys.decorate.InvocationDecoratorSupport;
import com.thoughtworks.proxy.toys.delegate.DelegatingInvoker;
import com.thoughtworks.proxy.toys.delegate.DelegationException;
import com.thoughtworks.xjb.jndi.XjbInitialContextFactory;
import com.thoughtworks.xjb.log.Log;
import com.thoughtworks.xjb.log.LogFactory;

/**
 * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
 */
public class XjbRemoteFactory implements RemoteFactory {
    
    private static final Log log = LogFactory.getLog(XjbRemoteFactory.class);

	/**
     * Replace any <tt>DelegationException</tt> with a <tt>RemoteException</tt>
     */
    private static class RemoteDecorator extends InvocationDecoratorSupport {
		public Throwable decorateException(Throwable cause) {
          if (cause instanceof DelegationException) {
              DelegationException oops = (DelegationException) cause;
              return new RemoteException(oops.getMessage(), oops.getCause());
          }
          else {
              return cause;
          }
      }
	}

	private static class EJBObjectInvoker extends DelegatingInvoker {
        private final String ejbName;
        private final EJBHome ejbHome;
        
        public EJBObjectInvoker(String ejbName, EJBHome ejbHome, Object impl) {
            super(impl);
            this.ejbName = ejbName;
            this.ejbHome = ejbHome;
        }
        
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            String oldContextName = XjbInitialContextFactory.getLocalContextName();
            try {
                Object result;
	            XjbInitialContextFactory.setLocalContext(ejbName);
                if (isEjbMethod("remove", method)) {
                    result = invokeOnDelegate(getDelegateMethod("ejbRemove", null), args);
                }
                else if (isEjbMethod("getEJBHome", method)) {
                    result = ejbHome;
                }
                else if (isEjbMethod("isIdentical", method)) {
                    result = Boolean.valueOf(proxy == args[0]);
                }
                else {
                    result = super.invoke(proxy, method, args);
                }
                return result;
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
    
	public EJBObject createRemote(String ejbName, EJBHome ejbHome, Class remoteInterface, Object impl) {
		final Invoker ejbInvoker = new EJBObjectInvoker(ejbName, ejbHome, impl);
        final Invoker remoteInvoker = new DecoratingInvoker(ejbInvoker, new RemoteDecorator());
        
		final EJBObject result =
             (EJBObject) new StandardProxyFactory().createProxy(new Class[]{remoteInterface}, remoteInvoker);
		if (impl instanceof SessionBean) {
			try {
				((SessionBean) impl).setSessionContext(createSessionContext(ejbHome, result));
			} catch (RemoteException e) {
				throw new RuntimeException(
						"Problem calling setSessionContext for " + ejbName + ": " + e.getMessage());
			}
		}
        else {
            log.debug("Not a session bean: " + impl);
        }
		return result;
	}

	protected XjbSessionContext createSessionContext(EJBHome ejbHome, final EJBObject remote) {
		return new XjbSessionContext(ejbHome, remote);
	}
}
