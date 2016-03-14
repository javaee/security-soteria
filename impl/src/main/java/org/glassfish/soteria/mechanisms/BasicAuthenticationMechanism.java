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
package org.glassfish.soteria.mechanisms;

import static java.lang.String.format;
import static javax.security.identitystore.CredentialValidationResult.Status.VALID;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static org.glassfish.soteria.Utils.isEmpty;

import javax.enterprise.inject.spi.CDI;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.authentication.mechanism.http.HttpMessageContext;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.credential.Password;
import javax.security.identitystore.credential.UsernamePasswordCredential;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Authentication mechanism that authenticates using basic authentication
 * 
 * @author Arjan Tijms
 *
 */
public class BasicAuthenticationMechanism implements HttpAuthenticationMechanism {
	
    private final String basicHeaderValue;
    
    public BasicAuthenticationMechanism(String realmName) {
        this.basicHeaderValue = format("Basic realm=\"%s\"", realmName);
    }
    
	@Override
	public AuthStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMsgContext) throws AuthException {
		
		String[] credentials = getCredentials(request);
		if (!isEmpty(credentials)) {
		    
		    IdentityStore identityStore = CDI.current().select(IdentityStore.class).get();
		    
		    CredentialValidationResult result = identityStore.validate(
                new UsernamePasswordCredential(credentials[0], new Password(credentials[1])));

            if (result.getStatus() == VALID) {
                return httpMsgContext.notifyContainerAboutLogin(
                    result.getCallerPrincipal(), result.getCallerGroups());
			}		
		}
		
		if (httpMsgContext.isProtected()) {
			response.setHeader("WWW-Authenticate", basicHeaderValue);
			return httpMsgContext.responseUnAuthorized();
		}
		
		return httpMsgContext.doNothing();
	}
	
	private String[] getCredentials(HttpServletRequest request) {
		
		String authorizationHeader = request.getHeader("Authorization");
		if (!isEmpty(authorizationHeader) && authorizationHeader.startsWith("Basic ") ) {
			return new String(parseBase64Binary(authorizationHeader.substring(6))).split(":");
		}
		
		return null;
	}

}