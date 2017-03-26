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
import static javax.security.auth.message.AuthStatus.SUCCESS;
import static org.glassfish.soteria.Utils.isImplementationOf;
import static org.glassfish.soteria.Utils.validateRequestMethod;

import java.io.Serializable;
import java.security.Principal;

import javax.annotation.Priority;
import javax.el.ELProcessor;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.security.auth.callback.Callback;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.authentication.mechanism.http.HttpMessageContext;
import javax.security.authentication.mechanism.http.annotation.AutoApplySession;
import javax.servlet.http.HttpServletRequest;

@Interceptor
@AutoApplySession
@Priority(PLATFORM_BEFORE + 200)
public class AutoApplySessionInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        
        if (isImplementationOf(invocationContext.getMethod(), validateRequestMethod)) {
            
            HttpMessageContext httpMessageContext = (HttpMessageContext)invocationContext.getParameters()[2];
            
            Principal userPrincipal = getPrincipal(httpMessageContext.getRequest());
            
            if (userPrincipal != null) {
                
                httpMessageContext.getHandler().handle(new Callback[] { 
                    new CallerPrincipalCallback(httpMessageContext.getClientSubject(), userPrincipal) }
                );
                         
                return SUCCESS;
            }
            
            Object outcome = invocationContext.proceed();
            
            if (SUCCESS.equals(outcome)) {
                httpMessageContext.getMessageInfo().getMap().put("javax.servlet.http.registerSession", TRUE.toString());
            }
            
            return outcome;
        }
        
        return invocationContext.proceed();
    }
    
    // TEMP workaround for https://github.com/payara/Payara/issues/290#issuecomment-207136005
    // TODO: THIS HAS TO BE FIXED AND SHOULD NOT STAY IN THE CODE LIKE THIS
    private Principal getPrincipal(HttpServletRequest request) {
        
        try {
            ELProcessor elProcessor = new ELProcessor();
            elProcessor.defineBean("request", request);
            return (Principal) elProcessor.eval("request.getUnwrappedCoyoteRequest().getUserPrincipal()");
        } catch (Exception e) {
            // ignore
        }
        
        return request.getUserPrincipal();
        
    }
    
}