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
package org.glassfish.soteria.test;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.security.auth.message.AuthStatus.FAILURE;
import static javax.security.auth.message.AuthStatus.SEND_CONTINUE;
import static javax.security.auth.message.AuthStatus.SEND_FAILURE;
import static javax.security.authentication.mechanism.http.AuthenticationParameters.withParams;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.SecurityContext;
import javax.security.auth.message.AuthStatus;
import javax.security.identitystore.credential.Credential;
import javax.security.identitystore.credential.Password;
import javax.security.identitystore.credential.UsernamePasswordCredential;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Named
@RequestScoped
public class LoginBacking {
    
    @Inject
    private SecurityContext securityContext;

    @NotNull
    @Size(min = 3, max = 15, message="Username must be between 3 and 15 characters")
    private String username;
    
    @NotNull
    @Size(min = 5, max = 50, message="Password must be between 5 and 50 characters")
    private String password;
    
    public void login() {
        
        FacesContext context = FacesContext.getCurrentInstance();
        Credential credential = new UsernamePasswordCredential(username, new Password(password));
        
        AuthStatus status = securityContext.authenticate(
            getRequest(context),
            getResponse(context), 
            withParams()
                .credential(credential));
        
        if (status.equals(SEND_CONTINUE)) {
            // Authentication mechanism has send a redirect, should not
            // send anything to response from JSF now.
            context.responseComplete();
        } else if (status.equals(SEND_FAILURE) || status.equals(FAILURE)) {
            // TODO: only 1 status should be returned. Change to enum or fix source.
            addError(context, "Authentication failed");
        }
        
    }
    
    private static HttpServletResponse getResponse(FacesContext context) {
        return (HttpServletResponse) context
            .getExternalContext()
            .getResponse();
    }
    
    private static HttpServletRequest getRequest(FacesContext context) {
        return (HttpServletRequest) context
            .getExternalContext()
            .getRequest();
    }
    
    private static void addError(FacesContext context, String message) {
        context
            .addMessage(
                null, 
                new FacesMessage(SEVERITY_ERROR, message, null));
    }

    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
