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