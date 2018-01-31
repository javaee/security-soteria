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

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;
import static org.glassfish.soteria.test.Utils.unmodifiableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.LdapIdentityStoreDefinition;

/**
 *
 */
@LdapIdentityStoreDefinition(
    url = "ldap://localhost:33389/",
    callerBaseDn = "ou=caller,dc=jsr375,dc=net",
    groupSearchBase = "ou=group,dc=jsr375,dc=net",
    useForExpression = "#{'VALIDATE'}" // usage of expression just for test
)
@ApplicationScoped
public class GroupProviderIdentityStore implements IdentityStore {

    private Map<String, Set<String>> groupsPerCaller;

    @PostConstruct
    public void init() {
        groupsPerCaller = new HashMap<>();

        groupsPerCaller.put("rudy", new HashSet<>(asList("foo", "bar")));
        groupsPerCaller.put("will", new HashSet<>(asList("foo", "bar", "baz")));
        groupsPerCaller.put("arjan", new HashSet<>(asList("foo", "baz")));
        groupsPerCaller.put("reza", new HashSet<>(asList("baz")));

    }

    @Override
    public Set<String> getCallerGroups(CredentialValidationResult validationResult) {
        Set<String> result = groupsPerCaller.get(validationResult.getCallerPrincipal().getName());
        if (result == null) {
            result = emptySet();
        }

        return result;
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return unmodifiableSet(PROVIDE_GROUPS);
    }
}
