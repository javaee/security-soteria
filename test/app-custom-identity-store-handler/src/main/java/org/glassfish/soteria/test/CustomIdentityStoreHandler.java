/*
 * Copyright (c) 2015-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.glassfish.soteria.test;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.IdentityStoreHandler;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.VALIDATE;
import static org.glassfish.soteria.test.CdiUtils.getBeanReferencesByType;

/**
 *
 */
@Alternative
@Priority(APPLICATION)
@ApplicationScoped
public class CustomIdentityStoreHandler implements IdentityStoreHandler {

    private List<IdentityStore> validatingIdentityStores;
    private List<IdentityStore> groupProvidingIdentityStores;

    @PostConstruct
    public void init() {
        List<IdentityStore> identityStores = getBeanReferencesByType(IdentityStore.class, false);

        validatingIdentityStores = identityStores.stream()
                .filter(i -> i.validationTypes().contains(VALIDATE))
                .sorted(comparing(IdentityStore::priority))
                .collect(toList());

        groupProvidingIdentityStores = identityStores.stream()
                .filter(i -> i.validationTypes().contains(PROVIDE_GROUPS))
                .sorted(comparing(IdentityStore::priority))
                .collect(toList());
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        CredentialValidationResult validationResult = null;
        IdentityStore identityStore = null;

        // Check all stores and stop when one marks it as invalid.
        for (IdentityStore authenticationIdentityStore : validatingIdentityStores) {
            CredentialValidationResult temp = authenticationIdentityStore.validate(credential);
            switch (temp.getStatus()) {

                case NOT_VALIDATED:
                    // Don't do anything
                    break;
                case INVALID:
                    validationResult = temp;
                    break;
                case VALID:
                    validationResult = temp;
                    identityStore = authenticationIdentityStore;
                    break;
                default:
                    throw new IllegalArgumentException("Value not supported " + temp.getStatus());
            }
            if (validationResult != null && validationResult.getStatus() == CredentialValidationResult.Status.INVALID) {
                break;
            }
        }

        if (validationResult == null) {
            // No authentication store at all
            return INVALID_RESULT;
        }

        if (validationResult.getStatus() != VALID) {
            // No store validated (authenticated), no need to continue
            return validationResult;
        }

        CallerPrincipal callerPrincipal = validationResult.getCallerPrincipal();

        Set<String> groups = new HashSet<>();
        if (identityStore.validationTypes().contains(PROVIDE_GROUPS)) {
            groups.addAll(validationResult.getCallerGroups());
        }

        // Ask all stores that were configured for authorization to get the groups for the
        // authenticated caller
        for (IdentityStore authorizationIdentityStore : groupProvidingIdentityStores) {
            groups.addAll(authorizationIdentityStore.getCallerGroups(validationResult));
        }

        return new CredentialValidationResult(callerPrincipal, groups);

    }
}
