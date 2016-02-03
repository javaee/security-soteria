package org.glassfish.soteria.identitystores;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static javax.security.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;
import static javax.security.identitystore.CredentialValidationResult.Status.VALID;

import java.util.Map;

import javax.security.CallerPrincipal;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.annotation.Credentials;
import javax.security.identitystore.credential.Credential;
import javax.security.identitystore.credential.UsernamePasswordCredential;

public class EmbeddedIdentityStore implements IdentityStore {

    private Map<String, Credentials> callerToCredentials;

    public EmbeddedIdentityStore(Credentials[] credentials) {
        callerToCredentials = stream(credentials).collect(toMap(
            e -> e.callerName(), 
            e -> e)
        );
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            return validate((UsernamePasswordCredential) credential);
        }

        return NOT_VALIDATED_RESULT;
    }

    public CredentialValidationResult validate(UsernamePasswordCredential usernamePasswordCredential) {
        Credentials credentials = callerToCredentials.get(usernamePasswordCredential.getCaller());

        if (credentials != null && usernamePasswordCredential.getPassword().compareTo(credentials.password())) {
            return new CredentialValidationResult(
                VALID, 
                new CallerPrincipal(credentials.callerName()), 
                asList(credentials.groups())
            );
        }

        return INVALID_RESULT;
    }

}
