package org.glassfish.soteria.test;

import static java.util.Arrays.asList;
import static javax.security.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;

import javax.enterprise.context.RequestScoped;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.credential.Credential;
import javax.security.identitystore.credential.UsernamePasswordCredential;

@RequestScoped
public class TestBackupIdentityStore implements IdentityStore {

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            return validate((UsernamePasswordCredential) credential);
        }

        return NOT_VALIDATED_RESULT;
    }

    public CredentialValidationResult validate(UsernamePasswordCredential usernamePasswordCredential) {

        if (usernamePasswordCredential.getCaller().equals("reza") &&
                usernamePasswordCredential.getPassword().compareTo("secret2")) {

            return new CredentialValidationResult("reza", asList("foo", "bar"));
        }
        
        if (usernamePasswordCredential.getCaller().equals("alex") &&
                usernamePasswordCredential.getPassword().compareTo("verysecret")) {

            return new CredentialValidationResult("alex", asList("foo", "bar"));
        }

        return INVALID_RESULT;
    }
    
    public int priority() {
        return 20;
    }
}
