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