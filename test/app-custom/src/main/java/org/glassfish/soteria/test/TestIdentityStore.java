package org.glassfish.soteria.test;

import javax.enterprise.context.RequestScoped;
import javax.security.CallerPrincipal;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.credential.Credential;
import javax.security.identitystore.credential.UsernamePasswordCredential;

import static java.util.Arrays.asList;
import static javax.security.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;
import static javax.security.identitystore.CredentialValidationResult.Status.VALID;

@RequestScoped
public class TestIdentityStore implements IdentityStore {

    @Override
    public CredentialValidationResult validate(Credential credential, CallerPrincipal callerPrincipal) {
        if (credential instanceof UsernamePasswordCredential) {
            return validate((UsernamePasswordCredential) credential);
        }

        return NOT_VALIDATED_RESULT;
    }

    public CredentialValidationResult validate(UsernamePasswordCredential usernamePasswordCredential) {

        if (usernamePasswordCredential.getCaller().equals("reza") &&
                usernamePasswordCredential.getPassword().compareTo("secret1")) {

            return new CredentialValidationResult("reza", asList("foo", "bar"));
        }

        return INVALID_RESULT;
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public ValidationType validationType() {
        return ValidationType.BOTH;
    }
}
