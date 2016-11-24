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
package javax.security.authentication.mechanism.http;

import javax.security.identitystore.credential.Credential;

/**
 * Parameters that are provided along with an authentication request.
 * 
 * NOTICE: DESIGN MOCKUP / EARLY DRAFT
 *
 * @author Arjan Tijms
 *
 */
public class AuthenticationParameters {

    private Credential credential;

    // Signal to authentication mechanism an explicit new authentication dialog is wanted, as opposed to
    // continuing a potentially existing one
    private boolean newAuthentication;

    private boolean rememberMe; // draft idea; to let the caller indicate right away remember me is required. Or is this too specific?
    private boolean noPassword; // draft idea; for runAs and/or refresh authenticated identity purposes
    private String authMethod;

    private String redirectUrl; // draft idea; optional URL to redirect to after authentication succeeded

    public static AuthenticationParameters withParams() {
        return new AuthenticationParameters();
    }

    public AuthenticationParameters credential(Credential credential) {
        setCredential(credential);
        return this;
    }

    public AuthenticationParameters newAuthentication(boolean newAuthentication) {
        setNewAuthentication(newAuthentication);
        return this;
    }

    public AuthenticationParameters rememberMe(boolean rememberMe) {
        setRememberMe(rememberMe);
        return this;
    }

    public AuthenticationParameters noPassword(boolean noPassword) {
        setNoPassword(noPassword);
        return this;
    }

    public AuthenticationParameters authMethod(String authMethod) {
        setAuthMethod(authMethod);
        return this;
    }

    public AuthenticationParameters redirectUrl(String redirectUrl) {
        setRedirectUrl(redirectUrl);
        return this;
    }

    // Getters/setters

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public boolean isNewAuthentication() {
        return newAuthentication;
    }

    public void setNewAuthentication(boolean newAuthentication) {
        this.newAuthentication = newAuthentication;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public boolean isNoPassword() {
        return noPassword;
    }

    public void setNoPassword(boolean noPassword) {
        this.noPassword = noPassword;
    }

}