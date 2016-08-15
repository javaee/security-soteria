/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.soteria.cdi;

import org.junit.Before;
import org.junit.Test;

import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.credential.Credential;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 *
 */
public class DefaultIdentityStoreHandlerTest extends DefaultIdentityStoreHandler {

    private static final IdentityStore AUTHENTICATE_STORE = new IdentityStoreStrictAuthenticate();
    private static final IdentityStore VALID_STORE = new IdentityStoreStrictValid();
    private static final IdentityStore AUTHORIZATION_STORE = new IdentityStoreAdditionalGroup();
    private static final IdentityStore INVALID_STORE = new IdentityStoreStrictInvalid();
    private static final IdentityStore NOT_VALIDATED_STORE = new IdentityStoreStrictNotValidated();

    private static final String GROUP1 = "Group1";
    private static final String GROUP2 = "Group2";
    private static final String GROUP3 = "Group3";
    private static final String CALLER_NAME = "AuthenticateStore";

    private DefaultIdentityStoreHandler handler;

    @Before
    public void setup() {
        handler = new DefaultIdentityStoreHandler();

    }

    @Test
    public void validate_Invalid_1() {
        // Invalid satus should end the loop
        handler.identityStores = Arrays.asList(INVALID_STORE, VALID_STORE);

        CredentialValidationResult result = handler.validate(null);

        assertEquals("Status should be INVALID", CredentialValidationResult.Status.INVALID, result.getStatus());
        assertNull("CallerPrincipal must be null", result.getCallerPrincipal());
        assertNull("Groups should be null", result.getCallerGroups());

    }

    @Test
    public void validate_Invalid_2() {
        // Invalid satus should end the loop
        handler.identityStores = Arrays.asList(VALID_STORE, INVALID_STORE);

        CredentialValidationResult result = handler.validate(null);

        assertEquals("Status should be INVALID", CredentialValidationResult.Status.INVALID, result.getStatus());
        assertNull("CallerPrincipal must be null", result.getCallerPrincipal());
        assertNull("Groups should be null", result.getCallerGroups());

    }

    @Test
    public void validate_Authenticated() {
        // Authenticated is promoted to Valid.
        handler.identityStores = Arrays.asList(AUTHENTICATE_STORE);

        CredentialValidationResult result = handler.validate(null);

        assertEquals("Status should be VALID", CredentialValidationResult.Status.VALID, result.getStatus());
        assertEquals("Name of CallerPrincipal should be supplied name", CALLER_NAME, result.getCallerPrincipal().getName());
        assertTrue("Groups should be empty", result.getCallerGroups().isEmpty());

    }

    @Test
    public void validate_Valid() {
        // Multiple Valid stores can't be consulted
        handler.identityStores = Arrays.asList(VALID_STORE, AUTHORIZATION_STORE);

        CredentialValidationResult result = handler.validate(null);

        assertEquals("Status should be VALID", CredentialValidationResult.Status.VALID, result.getStatus());
        assertEquals("Name of CallerPrincipal should be supplied name", CALLER_NAME, result.getCallerPrincipal().getName());
        assertTrue("Groups should contain Group1, Group2 and Group3", result.getCallerGroups().equals(Arrays.asList(GROUP1, GROUP2, GROUP3)));

    }

    @Test
    public void validate_NotValidated() {
        // Not Validated is changed to Invalid
        handler.identityStores = Arrays.asList(NOT_VALIDATED_STORE);

        CredentialValidationResult result = handler.validate(null);

        assertEquals("Status should be INVALID", CredentialValidationResult.Status.INVALID, result.getStatus());
        assertNull("CallerPrincipal must be null", result.getCallerPrincipal());
        assertNull("Groups should be null", result.getCallerGroups());

    }

    private static class IdentityStoreStrictAuthenticate implements IdentityStore {

        @Override
        public CredentialValidationResult validate(CredentialValidationResult partialValidationResult, Credential credential) {
            return new CredentialValidationResult(partialValidationResult, "AuthenticateStore");
        }

        @Override
        public int priority() {
            return 10; // Not Used
        }
    }

    private static class IdentityStoreStrictValid implements IdentityStore {

        @Override
        public CredentialValidationResult validate(CredentialValidationResult partialValidationResult, Credential credential) {
            return new CredentialValidationResult(partialValidationResult, CALLER_NAME, Arrays.asList(GROUP1, GROUP2));
        }

        @Override
        public int priority() {
            return 10; // Not Used
        }
    }

    private static class IdentityStoreAdditionalGroup implements IdentityStore {

        @Override
        public CredentialValidationResult validate(CredentialValidationResult partialValidationResult, Credential credential) {
            return new CredentialValidationResult(partialValidationResult, "AuthenticateStore", Arrays.asList(GROUP3));
        }

        @Override
        public int priority() {
            return 10; // Not Used
        }
    }

    private static class IdentityStoreStrictInvalid implements IdentityStore {

        @Override
        public CredentialValidationResult validate(CredentialValidationResult partialValidationResult, Credential credential) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        @Override
        public int priority() {
            return 10; // Not Used
        }
    }

    private static class IdentityStoreStrictNotValidated implements IdentityStore {

        @Override
        public CredentialValidationResult validate(CredentialValidationResult partialValidationResult, Credential credential) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        @Override
        public int priority() {
            return 10; // Not Used
        }
    }
}

