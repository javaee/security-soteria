package org.glassfish.soteria.test;

import static javax.security.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.identitystore.CredentialValidationResult.Status.VALID;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.security.CallerPrincipal;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.RememberMeIdentityStore;
import javax.security.identitystore.credential.RememberMeCredential;

@ApplicationScoped
public class TestRememberMeIdentityStore implements RememberMeIdentityStore {

	private final Map<String, CredentialValidationResult> identities = new ConcurrentHashMap<>();
	
	@Override
	public CredentialValidationResult validate(RememberMeCredential credential) {
		if (identities.containsKey(credential.getToken())) {
			return identities.get(credential.getToken());
		}
		
		 return INVALID_RESULT;
	}
	
	@Override
	public String generateLoginToken(CallerPrincipal callerPrincipal, List<String> groups) {
		String token = UUID.randomUUID().toString();
		
		// NOTE: FOR EXAMPLE ONLY. AS TOKENKEY WOULD EFFECTIVELY BECOME THE REPLACEMENT PASSWORD
		// IT SHOULD NORMALLY NOT BE STORED DIRECTLY BUT EG USING STRONG HASHING
		identities.put(token, new CredentialValidationResult(VALID, callerPrincipal, groups));
		
		return token;
	}
	
	@Override
	public void removeLoginToken(String token) {
		identities.remove(token);
	}
	
}
