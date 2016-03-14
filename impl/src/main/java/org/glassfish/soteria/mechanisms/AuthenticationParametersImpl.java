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

import javax.security.authentication.mechanism.http.AuthenticationParameters;

/**
 * Parameters that are provided along with an authentication request.
 *
 * @author Arjan Tijms
 *
 */
public class AuthenticationParametersImpl implements AuthenticationParameters {

	private String username;
	private String password;
	private Boolean rememberMe;
	private Boolean noPassword;
	private String authMethod;

	private String redirectUrl;

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#username(java.lang.String)
     */
	@Override
    public AuthenticationParameters username(String username) {
		setUsername(username);
		return this;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#password(java.lang.String)
     */
	@Override
    public AuthenticationParameters password(String passWord) {
		setPassword(passWord);
		return this;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#rememberMe(boolean)
     */
	@Override
    public AuthenticationParameters rememberMe(boolean rememberMe) {
		setRememberMe(rememberMe);
		return this;
	}
	
	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#noPassword(boolean)
     */
	@Override
    public AuthenticationParameters noPassword(boolean noPassword) {
		setNoPassword(noPassword);
		return this;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#authMethod(java.lang.String)
     */
	@Override
    public AuthenticationParameters authMethod(String authMethod) {
		setAuthMethod(authMethod);
		return this;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#redirectUrl(java.lang.String)
     */
	@Override
    public AuthenticationParameters redirectUrl(String redirectUrl) {
		setRedirectUrl(redirectUrl);
		return this;
	}

	// Getters/setters

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#getUsername()
     */
	@Override
    public String getUsername() {
		return username;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#setUsername(java.lang.String)
     */
	@Override
    public void setUsername(String username) {
		this.username = username;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#getPassword()
     */
	@Override
    public String getPassword() {
		return password;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#setPassword(java.lang.String)
     */
	@Override
    public void setPassword(String password) {
		this.password = password;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#getRememberMe()
     */
	@Override
    public Boolean getRememberMe() {
		return rememberMe;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#setRememberMe(java.lang.Boolean)
     */
	@Override
    public void setRememberMe(Boolean rememberMe) {
		this.rememberMe = rememberMe;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#getAuthMethod()
     */
	@Override
    public String getAuthMethod() {
		return authMethod;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#setAuthMethod(java.lang.String)
     */
	@Override
    public void setAuthMethod(String authMethod) {
		this.authMethod = authMethod;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#getRedirectUrl()
     */
	@Override
    public String getRedirectUrl() {
		return redirectUrl;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#setRedirectUrl(java.lang.String)
     */
	@Override
    public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#getNoPassword()
     */
	@Override
    public Boolean getNoPassword() {
		return noPassword;
	}

	/* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.AuthenticationParameters#setNoPassword(java.lang.Boolean)
     */
	@Override
    public void setNoPassword(Boolean noPassword) {
		this.noPassword = noPassword;
	}

}