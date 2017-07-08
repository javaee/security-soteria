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
package org.glassfish.soteria.mechanisms;

import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.CDI;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.AutoApplySession;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import javax.security.enterprise.identitystore.IdentityStoreHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Authentication mechanism that authenticates according to the Servlet spec defined FORM
 * authentication mechanism. See Servlet spec for further details.
 * 
 * @author Arjan Tijms
 *
 */
@AutoApplySession // For "is user already logged-in"
@LoginToContinue  // Redirects to form page if protected resource and not-logged in
@Typed(CustomFormAuthenticationMechanism.class) // Omit HttpAuthenticationMechanism type so it won't qualify directly as mechanism
public class CustomFormAuthenticationMechanism implements HttpAuthenticationMechanism, LoginToContinueHolder {
	
    private LoginToContinue loginToContinue;
    
	@Override
	public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthenticationException {
        
        if (hasCredential(httpMessageContext)) {

            IdentityStoreHandler identityStoreHandler = CDI.current().select(IdentityStoreHandler.class).get();
            
            return httpMessageContext.notifyContainerAboutLogin(
                    identityStoreHandler.validate(
                    httpMessageContext.getAuthParameters()
                                      .getCredential()));
        }
		
		return httpMessageContext.doNothing();
	}
	
	private static boolean hasCredential(HttpMessageContext httpMessageContext) {
	    return 
            httpMessageContext.getAuthParameters().getCredential() != null;
	}
	
    public LoginToContinue getLoginToContinue() {
        return loginToContinue;
    }

    public void setLoginToContinue(LoginToContinue loginToContinue) {
        this.loginToContinue = loginToContinue;
    }
    
    public CustomFormAuthenticationMechanism loginToContinue(LoginToContinue loginToContinue) {
        setLoginToContinue(loginToContinue);
        return this;
    }

}