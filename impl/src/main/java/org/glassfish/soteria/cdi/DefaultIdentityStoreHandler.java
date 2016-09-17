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

import javax.security.CallerPrincipal;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.IdentityStoreHandler;
import javax.security.identitystore.credential.Credential;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class DefaultIdentityStoreHandler implements IdentityStoreHandler {

    // protected so that @Specialized CDI bean can access the identityStores.
    protected List<IdentityStore> identityStores;

    public void init() {
        identityStores = CdiUtils.getBeanReferencesByType(IdentityStore.class, false);
        identityStores.sort(Comparator.comparing(IdentityStore::priority));
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        CredentialValidationResult validationResult = CredentialValidationResult.NONE_RESULT;
        Iterator<IdentityStore> storeIterator = identityStores.iterator();
        CallerPrincipal callerResult = null;

        while (storeIterator.hasNext() && validationResult.getStatus() != CredentialValidationResult.Status.INVALID) {

            IdentityStore identityStore = storeIterator.next();
            if (shouldIdentityStoreBeCalled(identityStore, validationResult)) {
                CredentialValidationResult result = identityStore.validate(credential, callerResult);

                // INVALID -> Just take this result as it should stop the loop.
                if (result.getStatus() == CredentialValidationResult.Status.INVALID) {
                    validationResult = result;
                }

                // AUTHENTICATED /  VALID -> Combine with previous result.
                if (result.getStatus() == CredentialValidationResult.Status.VALID || result.getStatus() == CredentialValidationResult.Status.AUTHENTICATED) {
                    validationResult = new CredentialValidationResult(validationResult, result.getStatus(), result.getCallerPrincipal(), result.getCallerGroups());
                    callerResult = validationResult.getCallerPrincipal();  // Use the current CallerPrincipal for the next iteration
                }

                // Not validated should not be considered here as the IdentityStore didn't participate in the iteration.
            }
        }

        if (CredentialValidationResult.Status.AUTHENTICATED == validationResult.getStatus()) {
            // Make the status from Authenticated to Valid, not adding any group.
            validationResult = new CredentialValidationResult(validationResult, new ArrayList<>());
        }

        if (CredentialValidationResult.Status.NONE == validationResult.getStatus()) {
            // The store(s) we have, couldn't handle the type of Credentials. So assume it is Invalid.
            validationResult = CredentialValidationResult.INVALID_RESULT;
        }

        return validationResult;
    }

    private boolean shouldIdentityStoreBeCalled(IdentityStore identityStore, CredentialValidationResult validationResult) {
        boolean result = identityStore.validationType() == IdentityStore.ValidationType.BOTH || identityStore.validationType() == IdentityStore.ValidationType.AUTHENTICATION;
        if (!result && identityStore.validationType() == IdentityStore.ValidationType.AUTHORIZATION) {
            // When store does authorization only, we should have at least a successful Authentication
            result = validationResult.getStatus() == CredentialValidationResult.Status.AUTHENTICATED
                    || validationResult.getStatus() == CredentialValidationResult.Status.VALID;
        }

        return result;
    }

}
