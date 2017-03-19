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
package org.glassfish.soteria;

import static javax.security.auth.message.AuthStatus.FAILURE;
import static javax.security.auth.message.AuthStatus.SUCCESS;
import static org.glassfish.soteria.mechanisms.jaspic.Jaspic.getLastStatus;

import java.io.Serializable;
import java.security.Principal;

import javax.inject.Inject;
import javax.security.SecurityContext;
import javax.security.auth.message.AuthStatus;
import javax.security.authentication.mechanism.http.AuthenticationParameters;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.soteria.mechanisms.jaspic.Jaspic;

public class SecurityContextImpl implements SecurityContext, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject // Injection of HttpServletRequest doesn't work for TomEE
    private HttpServletRequest request;
    
    @Override
    public Principal getCallerPrincipal() {
    	// Temporary implementation. Eventually we'd like to have this usable in every context.
    	// Options to consider:
    	// Try both the request and an injected EJB bean in order
    	// Use JACC
    	// Depend on server specific code
    	// Note that the injected Principal from the CDI spec is troublesome as we can't
    	// cast it to a custom principal, should one be used.
        
        // Needs role mapper?
        
    	return request.getUserPrincipal();
    }
    
    @Override
    public boolean isCallerInRole(String role) {
    	// Temporary implementation. Eventually we'd like to have this usable in every context.
        // Query WebRoleRefPermission
    	return request.isUserInRole(role);
    }
    
    @Override
    public AuthStatus authenticate(HttpServletResponse response, AuthenticationParameters parameters) {
        return authenticate(request, response, parameters);
    }

    @Override
    public AuthStatus authenticate(HttpServletRequest request, HttpServletResponse response, AuthenticationParameters parameters) {
        
        try {
            if (Jaspic.authenticate(request, response, parameters)) {
                // All servers return true when authentication actually took place 
                return SUCCESS;
            }
            
            // GlassFish returns false when either authentication is in progress or authentication
            // failed (or was not done at all). 
            // Therefore we need to rely on the status we saved as a request attribute
            return getLastStatus(request);
        } catch (IllegalArgumentException e) { // TODO: exception type not ideal
            // JBoss returns false when authentication is in progress, but throws exception when
            // authentication fails (or was not done at all).
            return FAILURE;
        }
    }
    
}
