/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.soteria.cdi;

import static java.lang.Boolean.TRUE;
import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static org.glassfish.soteria.Utils.getBaseURL;
import static org.glassfish.soteria.Utils.getParam;
import static org.glassfish.soteria.Utils.isEmpty;
import static org.glassfish.soteria.Utils.isImplementationOf;
import static org.glassfish.soteria.Utils.notNull;
import static org.glassfish.soteria.Utils.validateRequestMethod;
import static org.glassfish.soteria.cdi.CdiUtils.getAnnotation;
import static org.glassfish.soteria.servlet.RequestCopier.copy;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.security.auth.message.AuthException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.glassfish.soteria.mechanisms.LoginToContinueHolder;
import org.glassfish.soteria.servlet.AuthenticationData;
import org.glassfish.soteria.servlet.HttpServletRequestDelegator;
import org.glassfish.soteria.servlet.RequestData;


@Interceptor
@LoginToContinue
@Priority(PLATFORM_BEFORE + 220)
public class LoginToContinueInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Inject
    private BeanManager beanManager;
    
    @Inject
    @Intercepted
    private Bean<?> interceptedBean;

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        
        // If intercepting HttpAuthenticationMechanism#validateRequest
        if (isImplementationOf(invocationContext.getMethod(), validateRequestMethod)) {
            return validateRequest(
                invocationContext, 
                getParam(invocationContext, 0),  
                getParam(invocationContext, 1),
                getParam(invocationContext, 2));
        }
        
        return invocationContext.proceed();
    }
    
    private AuthenticationStatus validateRequest(InvocationContext invocationContext, HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws Exception {
        
        // Check if there's any state lingering behind from a previous aborted authentication dialog
        tryClean(httpMessageContext);
        
        if (isCallerInitiatedAuthentication(request)) {
            // The caller explicitly initiated the authentication dialog, i.e. by clicking on a login button,
            // in response to which the application called HttpServletRequest#authenticate
            return processCallerInitiatedAuthentication(invocationContext, request, response, httpMessageContext);
        } else {
            // If the caller didn't initiated the dialog, the container did, i.e. after the caller tried to access
            // a protected resource.
            return processContainerInitiatedAuthentication(invocationContext, request, response, httpMessageContext);
        }
    }
    
    private void tryClean(HttpMessageContext httpMessageContext) {
        
        // 1. Check if caller aborted earlier flow and does a new request to protected resource
        if (isOnProtectedURLWithStaleData(httpMessageContext)) {
            removeSavedRequest(httpMessageContext.getRequest());
            removeCallerInitiatedAuthentication(httpMessageContext.getRequest());
        }
        
        // 2. Check if caller aborted earlier flow and explicitly initiated a new authentication dialog 
        if (httpMessageContext.getAuthParameters().isNewAuthentication()) {
            saveCallerInitiatedAuthentication(httpMessageContext.getRequest());
            removeSavedRequest(httpMessageContext.getRequest());
            removeSavedAuthentication(httpMessageContext.getRequest());
        }
    }
    
    private AuthenticationStatus processCallerInitiatedAuthentication(InvocationContext invocationContext, HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws Exception {
        // Try to authenticate with the next interceptor or actual authentication mechanism
        AuthenticationStatus authstatus;
        
        try {
            authstatus = (AuthenticationStatus) invocationContext.proceed();
        } catch (AuthException e) {
            authstatus = AuthenticationStatus.SEND_FAILURE;
        }
        
        if (authstatus == AuthenticationStatus.SUCCESS) {
            
            if (httpMessageContext.getCallerPrincipal() == null) {
                return AuthenticationStatus.SUCCESS;
            }
            
            // Actually authenticated now, so we remove the authentication dialog marker
            removeCallerInitiatedAuthentication(httpMessageContext.getRequest());
            
            // TODO: for some mechanisms, such as OAuth the caller would now likely be at an
            // application OAuth landing page, and should likely be returned to "some other" location
            // (e.g. the page from which a login link was clicked in say a top menu bar)
            //
            // Do we add support for this, e.g. via a watered down savedRequest (saving only a caller provided URL)
            // Or do we leave this as an application responsibility?
        }
        
        return authstatus;
    }
    
    private AuthenticationStatus processContainerInitiatedAuthentication(InvocationContext invocationContext, HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws Exception {

        // 1. Protected resource requested and no request saved before
        if (isOnInitialProtectedURL(httpMessageContext)) {
            // TODO: request.authenticate() is captured by this as well
            // Use an "initial call tracker interceptor"?
            
            // Save request details and redirect/forward to /login page
            saveRequest(request);
            
            LoginToContinue loginToContinueAnnotation = getLoginToContinueAnnotation(invocationContext);
            
            // TODO: Use modified request/response for forward to set method to GET and filter out "if-" headers?
            
            if (loginToContinueAnnotation.useForwardToLogin()) {
                return httpMessageContext.forward(
                    loginToContinueAnnotation.loginPage());
            } else {
                return httpMessageContext.redirect(
                    getBaseURL(request) + loginToContinueAnnotation.loginPage());
            }
        }
        
        
        // 2. A postback after we have redirected the caller in step 1.
        //    NOTE: this does not have to be the resource we redirected the caller to.
        //          E.g. we can redirect to /login, and /login can postback to J_SECURITY_CHECK or /login2,
        //          or whatever. For each such postback we give the authentication mechanism the opportunity
        //          to authenticate though.
        if (isOnLoginPostback(request)) {
            // Try to authenticate with the next interceptor or actual authentication mechanism
            AuthenticationStatus authstatus;
            
            try {
                authstatus = (AuthenticationStatus) invocationContext.proceed();
            } catch (AuthException e) {
                authstatus = AuthenticationStatus.SEND_FAILURE;
            }
          
            // (Following the JASPIC spec (3.8.3.1) validateRequest before service invocation can only return 
            // SUCCESS, SEND_CONTINUE, SEND_FAILURE or throw an exception
            if (authstatus == AuthenticationStatus.SUCCESS) {
                
                if (httpMessageContext.getCallerPrincipal() == null) {
                    return AuthenticationStatus.SUCCESS;
                }
                
                // Authentication was successful and an actual caller principal was set 
                RequestData savedRequest = getSavedRequest(request);
                
                // Check if we're already on the right target URL
                if  (!savedRequest.matchesRequest(request)) {
                
                    // Store the authenticated data before redirecting to the right
                    // URL. This is needed since the underlying JASPIC runtime does not
                    // remember the authenticated identity if we redirect.
                    saveAuthentication(request, new AuthenticationData(
                            httpMessageContext.getCallerPrincipal(),
                            httpMessageContext.getGroups()));
                    
                    return httpMessageContext.redirect(savedRequest.getFullRequestURL());
                } // else return success
                
            } else if (authstatus == AuthenticationStatus.SEND_FAILURE)  {
                
                String errorPage = getLoginToContinueAnnotation(invocationContext).errorPage();
                
                if (isEmpty(errorPage)) {
                    return authstatus;
                }
                
                return httpMessageContext.redirect( // TODO: optionally forward?
                    getBaseURL(request) + errorPage);
            } else {
                // Basically SEND_CONTINUE
                return authstatus;
            }
             
        }
        
        
        // 3. Authenticated data saved and back on original URL from step 1.
        if (isOnOriginalURLAfterAuthenticate(request)) {
            
            // Remove all the data we saved
            RequestData requestData = removeSavedRequest(request);
            AuthenticationData authenticationData = removeSavedAuthentication(request);
            
            // Wrap the request to provide all the original request data again, such as the original
            // headers and the HTTP method, authenticate and then invoke the originally requested resource
            return httpMessageContext
                .withRequest(new HttpServletRequestDelegator(request, requestData))
                .notifyContainerAboutLogin(
                    authenticationData.getPrincipal(), 
                    authenticationData.getGroups());
            
        }
       
        return (AuthenticationStatus) invocationContext.proceed();

    }
    
    private boolean isCallerInitiatedAuthentication(HttpServletRequest request) {
        return TRUE.equals(getCallerInitiatedAuthentication(request));
    }
    
    private boolean isOnProtectedURLWithStaleData(HttpMessageContext httpMessageContext) {
        return
            httpMessageContext.isProtected() && 
            
            // When HttpServletRequest#authenticate is called, it counts as "mandated" authentication
            // which here means isProtected() is true. But we want to use HttpServletRequest#authenticate
            // to resume a dialog started by accessing a protected page, so therefore exclude it here.
            !httpMessageContext.isAuthenticationRequest() &&
            getSavedRequest(httpMessageContext.getRequest()) != null &&
            getSavedAuthentication(httpMessageContext.getRequest()) == null &&
           
            // Some servers consider the Servlet special URL "/j_security_check" as
            // a protected URL
            !httpMessageContext.getRequest().getRequestURI().endsWith("j_security_check");
    }
    
    private boolean isOnInitialProtectedURL(HttpMessageContext httpMessageContext) {
        return 
            httpMessageContext.isProtected() &&
            
            // When HttpServletRequest#authenticate is called, it counts as "mandated" authentication
            // which here means isProtected() is true. But we want to use HttpServletRequest#authenticate
            // to resume a dialog started by accessing a protected page, so therefore exclude it here.
            !httpMessageContext.isAuthenticationRequest() &&
            getSavedRequest(httpMessageContext.getRequest()) == null && 
            getSavedAuthentication(httpMessageContext.getRequest()) == null &&
                    
            // Some servers consider the Servlet special URL "/j_security_check" as
            // a protected URL
            !httpMessageContext.getRequest().getRequestURI().endsWith("j_security_check");
    }
    
    private boolean isOnLoginPostback(HttpServletRequest request) {
        return 
            getSavedRequest(request) != null &&
            getSavedAuthentication(request) == null;
    }
    
    private boolean isOnOriginalURLAfterAuthenticate(HttpServletRequest request) {
        
        RequestData savedRequest = getSavedRequest(request);
        AuthenticationData authenticationData = getSavedAuthentication(request);
        
        return
            notNull(savedRequest, authenticationData) && 
            savedRequest.matchesRequest(request);
        
    }
    
    private LoginToContinue getLoginToContinueAnnotation(InvocationContext invocationContext) {
        
        if (invocationContext.getTarget() instanceof LoginToContinueHolder) {
            return ((LoginToContinueHolder) invocationContext.getTarget()).getLoginToContinue();
        }
        
        Optional<LoginToContinue> optionalLoginToContinue = getAnnotation(beanManager, interceptedBean.getBeanClass(), LoginToContinue.class);
        if (optionalLoginToContinue.isPresent()) {
            return optionalLoginToContinue.get();
        }
        
        throw new IllegalStateException("@LoginToContinue not present on " + interceptedBean.getBeanClass());
    }
    
    private static final String ORIGINAL_REQUEST_DATA_SESSION_NAME = "org.glassfish.soteria.original.request";
    private static final String AUTHENTICATION_DATA_SESSION_NAME = "org.glassfish.soteria.authentication";
    private static final String CALLER_INITIATED_AUTHENTICATION_SESSION_NAME = "org.glassfish.soteria.caller_initiated_authentication";
    
    private void saveCallerInitiatedAuthentication(HttpServletRequest request) {
        request.getSession().setAttribute(CALLER_INITIATED_AUTHENTICATION_SESSION_NAME, TRUE);
    }
    
    private Boolean getCallerInitiatedAuthentication(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        return (Boolean) session.getAttribute(CALLER_INITIATED_AUTHENTICATION_SESSION_NAME);
    }
    
    private void removeCallerInitiatedAuthentication(HttpServletRequest request) {
        request.getSession().removeAttribute(CALLER_INITIATED_AUTHENTICATION_SESSION_NAME);
    }

    private void saveRequest(HttpServletRequest request) {
        request.getSession().setAttribute(ORIGINAL_REQUEST_DATA_SESSION_NAME, copy(request));
    }

    private RequestData getSavedRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return (RequestData) session.getAttribute(ORIGINAL_REQUEST_DATA_SESSION_NAME);
    }

    private RequestData removeSavedRequest(HttpServletRequest request) {
        RequestData requestData = getSavedRequest(request);
        
        request.getSession().removeAttribute(ORIGINAL_REQUEST_DATA_SESSION_NAME);
        
        return requestData;
    }
    
    private void saveAuthentication(HttpServletRequest request, AuthenticationData authenticationData) {
        request.getSession().setAttribute(AUTHENTICATION_DATA_SESSION_NAME, authenticationData);
    }

    private AuthenticationData getSavedAuthentication(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return (AuthenticationData) session.getAttribute(AUTHENTICATION_DATA_SESSION_NAME);
    }

    private AuthenticationData removeSavedAuthentication(HttpServletRequest request) {
        AuthenticationData authenticationData = getSavedAuthentication(request);
        
        request.getSession().removeAttribute(AUTHENTICATION_DATA_SESSION_NAME);
        
        return authenticationData;
    }
 
}