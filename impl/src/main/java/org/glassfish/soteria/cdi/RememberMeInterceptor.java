/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static javax.security.auth.message.AuthStatus.SUCCESS;
import static javax.security.identitystore.CredentialValidationResult.Status.VALID;
import static org.glassfish.soteria.Utils.cleanSubjectMethod;
import static org.glassfish.soteria.Utils.getParam;
import static org.glassfish.soteria.Utils.isEmpty;
import static org.glassfish.soteria.Utils.isImplementationOf;
import static org.glassfish.soteria.Utils.validateRequestMethod;
import static org.glassfish.soteria.cdi.CdiUtils.getAnnotation;
import static org.glassfish.soteria.servlet.CookieHandler.getCookie;
import static org.glassfish.soteria.servlet.CookieHandler.removeCookie;
import static org.glassfish.soteria.servlet.CookieHandler.saveCookie;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Priority;
import javax.el.ELProcessor;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.security.auth.message.AuthStatus;
import javax.security.authentication.mechanism.http.HttpMessageContext;
import javax.security.authentication.mechanism.http.annotation.RememberMe;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.RememberMeIdentityStore;
import javax.security.identitystore.credential.RememberMeCredential;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Interceptor
@RememberMe
@Priority(PLATFORM_BEFORE + 210)
public class RememberMeInterceptor implements Serializable {

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
        
        // If intercepting HttpAuthenticationMechanism#cleanSubject
        if (isImplementationOf(invocationContext.getMethod(), cleanSubjectMethod)) {
            cleanSubject(
                invocationContext, 
                getParam(invocationContext, 0),  
                getParam(invocationContext, 1),
                getParam(invocationContext, 2));
        }
        
        return invocationContext.proceed();
    }
    
    private AuthStatus validateRequest(InvocationContext invocationContext, HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws Exception {
        
        RememberMeIdentityStore rememberMeIdentityStore = CDI.current().select(RememberMeIdentityStore.class).get(); // TODO ADD CHECKS
        RememberMe rememberMeAnnotation = getRememberMeFromIntercepted();
        
        Cookie rememberMeCookie = getCookie(request, rememberMeAnnotation.cookieName());
        
        if (rememberMeCookie != null) {
            
            // There's a remember me cookie, see if we can use it to authenticate
            
            CredentialValidationResult result = rememberMeIdentityStore.validate(
                new RememberMeCredential(rememberMeCookie.getValue())
            );
            
            if (result.getStatus() == VALID) {
                // The remember me store contained an authenticated identity associated with 
                // the given token, use it to authenticate with the container
                return httpMessageContext.notifyContainerAboutLogin(
                    result.getCallerPrincipal(), result.getCallerGroups());
            } else {
                // The token appears to be no longer valid, or perhaps wasn't valid
                // to begin with. Remove the cookie.
                removeCookie(request, response, rememberMeAnnotation.cookieName());
            }
        }
        
        // Try to authenticate with the next interceptor or actual authentication mechanism
        AuthStatus authstatus = (AuthStatus) invocationContext.proceed();
        
        if (authstatus == SUCCESS && httpMessageContext.getCallerPrincipal() != null) {
            
            // Authentication succeeded;
            // Check if remember me is wanted by the caller and if so
            // store the authenticated identity in the remember me store
            // and send a cookie with a token that can be used
            // to retrieve this stored identity later
            
            Boolean isRememberMe = true;
            if (!isEmpty(rememberMeAnnotation.isRememberMeExpression())) {
                ELProcessor elProcessor = getElProcessor(invocationContext, httpMessageContext);
                
                isRememberMe = (Boolean) elProcessor.eval(rememberMeAnnotation.isRememberMeExpression());
            }
            
            if (isRememberMe) {
                String token = rememberMeIdentityStore.generateLoginToken(
                    httpMessageContext.getCallerPrincipal(),
                    httpMessageContext.getGroups()
                );
                
                saveCookie(request, response, rememberMeAnnotation.cookieName(), token, rememberMeAnnotation.cookieMaxAgeSeconds());
            }
        }
        
        return authstatus;
    }
    
    private void cleanSubject(InvocationContext invocationContext, HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
    
        RememberMeIdentityStore rememberMeIdentityStore = CDI.current().select(RememberMeIdentityStore.class).get(); // TODO ADD CHECKS
        RememberMe rememberMeAnnotation = getRememberMeFromIntercepted();
        
        Cookie rememberMeCookie = getCookie(request, rememberMeAnnotation.cookieName());
        
        if (rememberMeCookie != null) {
            
            // There's a remember me cookie, remove the cookie
            removeCookie(request, response, rememberMeAnnotation.cookieName());
            
            // And remove the token (and with it the authenticated identity) from the store
            rememberMeIdentityStore.removeLoginToken(rememberMeCookie.getValue());
        }
        
    }
    
    private RememberMe getRememberMeFromIntercepted() {
        Optional<RememberMe> optionalRememberMe = getAnnotation(beanManager, interceptedBean.getBeanClass(), RememberMe.class);
        if (optionalRememberMe.isPresent()) {
            return optionalRememberMe.get();
        }
        
        throw new IllegalStateException("@RememberMe not present on " + interceptedBean.getBeanClass());
    }
    
    private ELProcessor getElProcessor(InvocationContext invocationContext, HttpMessageContext httpMessageContext) {
        ELProcessor elProcessor = new ELProcessor();
        
        elProcessor.getELManager().addELResolver(beanManager.getELResolver());
        elProcessor.defineBean("this", invocationContext.getTarget()); // deprecate/remove, not compatible with Apache EL
        elProcessor.defineBean("self", invocationContext.getTarget());
        elProcessor.defineBean("httpMessageContext", httpMessageContext);
        
        return elProcessor;
    }
 
}