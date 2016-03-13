package org.glassfish.soteria.mechanisms;

import static javax.security.identitystore.CredentialValidationResult.Status.VALID;
import static org.glassfish.soteria.Utils.notNull;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.authentication.mechanism.http.HttpMessageContext;
import javax.security.authentication.mechanism.http.annotation.AutoApplySession;
import javax.security.authentication.mechanism.http.annotation.LoginToContinue;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.credential.Password;
import javax.security.identitystore.credential.UsernamePasswordCredential;
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
@Typed(FormAuthenticationMechanism.class) // Omit HttpAuthenticationMechanism type so it won't qualify directly as mechanism
public class FormAuthenticationMechanism implements HttpAuthenticationMechanism, LoginToContinueHolder {
	
    private LoginToContinue loginToContinue;

    @Inject
    private IdentityStore identityStore;
    
	@Override
	public AuthStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthException {
		
	    if ("POST".equals(request.getMethod()) && request.getRequestURI().endsWith("/j_security_check")) {
	        
	        if (notNull(request.getParameter("j_username"), request.getParameter("j_password"))) {

	            CredentialValidationResult result = identityStore.validate(
	                new UsernamePasswordCredential(
                        request.getParameter("j_username"), 
                        new Password(request.getParameter("j_password"))));

	            if (result.getStatus() == VALID) {
	                return httpMessageContext.notifyContainerAboutLogin(
	                    result.getCallerPrincipal(), 
	                    result.getCallerGroups());
	                
	            } else {
	                throw new AuthException("Login failed");
	            }
	        } 
	    }
		
		return httpMessageContext.doNothing();
	}
	
    public LoginToContinue getLoginToContinue() {
        return loginToContinue;
    }

    public void setLoginToContinue(LoginToContinue loginToContinue) {
        this.loginToContinue = loginToContinue;
    }
    
    public FormAuthenticationMechanism loginToContinue(LoginToContinue loginToContinue) {
        setLoginToContinue(loginToContinue);
        return this;
    }

}