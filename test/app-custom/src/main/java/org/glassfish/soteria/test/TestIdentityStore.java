package org.glassfish.soteria.test;

import static java.util.Arrays.asList;
import static javax.security.identitystore.CredentialValidationResult.INVALID_RESULT;

import javax.enterprise.context.RequestScoped;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.credential.UsernamePasswordCredential;

@RequestScoped
public class TestIdentityStore implements IdentityStore {

    public CredentialValidationResult validate(UsernamePasswordCredential usernamePasswordCredential) {

        if (usernamePasswordCredential.compareTo("reza", "secret1")) {
            return new CredentialValidationResult("reza", asList("foo", "bar"));
        }

        return INVALID_RESULT;
    }
	
}
