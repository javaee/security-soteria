/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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
package javax.security.identitystore;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
public class CredentialValidationResultTest {

    private static final String CALLER_NAME = "JUnit";
    private static final String OTHER_CALLER_NAME = "OriginalCaller";

    private static final String GROUP1 = "Group1";
    private static final String GROUP2 = "Group2";
    private static final String GROUP3 = "Group3";

    private List<String> originalGroups;
    private List<String> additionalGroups;

    @Before
    public void setup() {
        originalGroups = Arrays.asList(GROUP1, GROUP2);
        additionalGroups = Arrays.asList(GROUP3);
    }

    /*
    @Test
    public void construction_Authenticated_None() {
        CredentialValidationResult partialResult = CredentialValidationResult.NONE_RESULT;
        CredentialValidationResult validationResult = new CredentialValidationResult(partialResult, CALLER_NAME);

        assertEquals("Status should be AUTHENTICATED", CredentialValidationResult.Status.AUTHENTICATED, validationResult.getStatus());
        assertNotNull("CallerPrincipal can't be null", validationResult.getCallerPrincipal());
        assertEquals("Name of CallerPrincipal should be supplied name", CALLER_NAME, validationResult.getCallerPrincipal().getName());
        assertTrue("Groups should be empty", validationResult.getCallerGroups().isEmpty());
    }

    @Test
    public void construction_Authenticated_NotValidated() {
        CredentialValidationResult partialResult = CredentialValidationResult.NOT_VALIDATED_RESULT;
        CredentialValidationResult validationResult = new CredentialValidationResult(partialResult, CALLER_NAME);

        assertEquals("Status should be AUTHENTICATED", CredentialValidationResult.Status.AUTHENTICATED, validationResult.getStatus());
        assertNotNull("CallerPrincipal can't be null", validationResult.getCallerPrincipal());
        assertEquals("Name of CallerPrincipal should be supplied name", CALLER_NAME, validationResult.getCallerPrincipal().getName());
        assertTrue("Groups should be empty", validationResult.getCallerGroups().isEmpty());
    }

    @Test
    public void construction_Authenticated_Valid() {
        CredentialValidationResult partialResult = new CredentialValidationResult(CredentialValidationResult.NONE_RESULT, CALLER_NAME, originalGroups);
        assertEquals("Status should be VALID", CredentialValidationResult.Status.VALID, partialResult.getStatus());

        CredentialValidationResult validationResult = new CredentialValidationResult(partialResult, CALLER_NAME);

        assertEquals("Status should be VALID", CredentialValidationResult.Status.VALID, validationResult.getStatus());
        assertNotNull("CallerPrincipal can't be null", validationResult.getCallerPrincipal());
        assertEquals("Name of CallerPrincipal should be supplied name", CALLER_NAME, validationResult.getCallerPrincipal().getName());
        assertTrue("Groups should be empty", validationResult.getCallerGroups().isEmpty());
    }

    @Test
    public void construction_Valid_None() {
        CredentialValidationResult partialResult = CredentialValidationResult.NONE_RESULT;
        CredentialValidationResult validationResult = new CredentialValidationResult(partialResult, CALLER_NAME, originalGroups);

        assertEquals("Status should be VALID", CredentialValidationResult.Status.VALID, validationResult.getStatus());
        assertNotNull("CallerPrincipal can't be null", validationResult.getCallerPrincipal());
        assertEquals("Name of CallerPrincipal should be supplied name", CALLER_NAME, validationResult.getCallerPrincipal().getName());
        assertTrue("Groups should contain Group1 and Group2", validationResult.getCallerGroups().equals(Arrays.asList(GROUP1, GROUP2)));

    }

    @Test
    public void construction_Valid_NotValidated() {
        CredentialValidationResult partialResult = CredentialValidationResult.NOT_VALIDATED_RESULT;
        CredentialValidationResult validationResult = new CredentialValidationResult(partialResult, CALLER_NAME, originalGroups);

        assertEquals("Status should be VALID", CredentialValidationResult.Status.VALID, validationResult.getStatus());
        assertNotNull("CallerPrincipal can't be null", validationResult.getCallerPrincipal());
        assertEquals("Name of CallerPrincipal should be supplied name", CALLER_NAME, validationResult.getCallerPrincipal().getName());
        assertTrue("Groups should contain Group1 and Group2", validationResult.getCallerGroups().equals(Arrays.asList(GROUP1, GROUP2)));

    }

    @Test
    public void construction_Valid_Authenticated_1() {
        // We specify another caller then at the moment we did the authentication
        // Create the previous authenticated partial result.
        CredentialValidationResult partialResult = new CredentialValidationResult(CredentialValidationResult.NONE_RESULT, OTHER_CALLER_NAME);

        assertEquals("Status should be AUTHENTICATED", CredentialValidationResult.Status.AUTHENTICATED, partialResult.getStatus());

        CredentialValidationResult validationResult = new CredentialValidationResult(partialResult, CALLER_NAME, originalGroups);

        assertEquals("Status should be VALID", CredentialValidationResult.Status.VALID, validationResult.getStatus());
        assertNotNull("CallerPrincipal can't be null", validationResult.getCallerPrincipal());
        // With the constructor we have specified another caller name, so that one is used.
        assertEquals("Name of CallerPrincipal should be supplied name", CALLER_NAME, validationResult.getCallerPrincipal().getName());
        assertTrue("Groups should contain Group1 and Group2", validationResult.getCallerGroups().equals(Arrays.asList(GROUP1, GROUP2)));

    }

    @Test
    public void construction_Valid_Authenticated_2() {
        // We specify no caller, so should be taken from the partalResult

        // Create the previous authenticated partial result.
        CredentialValidationResult partialResult = new CredentialValidationResult(CredentialValidationResult.NONE_RESULT, OTHER_CALLER_NAME);

        assertEquals("Status should be AUTHENTICATED", CredentialValidationResult.Status.AUTHENTICATED, partialResult.getStatus());

        CredentialValidationResult validationResult = new CredentialValidationResult(partialResult, originalGroups);

        assertEquals("Status should be VALID", CredentialValidationResult.Status.VALID, validationResult.getStatus());
        assertNotNull("CallerPrincipal can't be null", validationResult.getCallerPrincipal());
        // With the constructor we have specified another caller name, so that one is used.
        assertEquals("Name of CallerPrincipal should be supplied name", OTHER_CALLER_NAME, validationResult.getCallerPrincipal().getName());
        assertTrue("Groups should contain Group1 and Group2", validationResult.getCallerGroups().equals(Arrays.asList(GROUP1, GROUP2)));

    }

    @Test
    public void construction_Valid_Valid() {
        // Add additional groups

        // Create the previous Valid partial result.
        CredentialValidationResult partialResult = new CredentialValidationResult(CredentialValidationResult.NONE_RESULT, OTHER_CALLER_NAME, originalGroups);

        assertEquals("Status should be VALID", CredentialValidationResult.Status.VALID, partialResult.getStatus());

        CredentialValidationResult validationResult = new CredentialValidationResult(partialResult, additionalGroups);

        assertEquals("Status should be VALID", CredentialValidationResult.Status.VALID, validationResult.getStatus());
        assertNotNull("CallerPrincipal can't be null", validationResult.getCallerPrincipal());
        // With the constructor we have specified another caller name, so that one is used.
        assertEquals("Name of CallerPrincipal should be supplied name", OTHER_CALLER_NAME, validationResult.getCallerPrincipal().getName());
        assertTrue("Groups should contain Group1, Group2 and Group3", validationResult.getCallerGroups().equals(Arrays.asList(GROUP1, GROUP2, GROUP3)));

    }

    */
}