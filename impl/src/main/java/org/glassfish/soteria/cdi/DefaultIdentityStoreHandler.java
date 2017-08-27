/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.IdentityStoreHandler;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.enterprise.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;
import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.INVALID;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.VALIDATE;
import static org.glassfish.soteria.cdi.CdiUtils.getBeanReferencesByType;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 */
public class DefaultIdentityStoreHandler implements IdentityStoreHandler {

    private List<IdentityStore> authenticationIdentityStores;
    private List<IdentityStore> authorizationIdentityStores;

    public void init() {
    	List<IdentityStore> identityStores = getBeanReferencesByType(IdentityStore.class, false);

    	authenticationIdentityStores = identityStores.stream()
    												 .filter(i -> i.validationTypes().contains(VALIDATE))
    												 .sorted(comparing(IdentityStore::priority))
    												 .collect(toList());

    	authorizationIdentityStores = identityStores.stream()
				 									.filter(i -> i.validationTypes().contains(PROVIDE_GROUPS) && !i.validationTypes().contains(VALIDATE))
		 											.sorted(comparing(IdentityStore::priority))
	 												.collect(toList());
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {

        CredentialValidationResult validationResult = null;
        IdentityStore identityStore = null;
        boolean isGotAnInvalidResult = false;

        // Check stores to authenticate until one succeeds.
        for (IdentityStore authenticationIdentityStore : authenticationIdentityStores) {
            validationResult = authenticationIdentityStore.validate(credential);
            if (validationResult.getStatus() == VALID) {
                identityStore = authenticationIdentityStore;
                break;
            }
            else if (validationResult.getStatus() == INVALID) {
                isGotAnInvalidResult = true;
            }
        }

        if (validationResult == null || validationResult.getStatus() != VALID) {
            // Didn't get a VALID result. If we got an INVALID result at any point,
            // return INVALID_RESULT. Otherwise, return NOT_VALIDATED_RESULT.
            if (isGotAnInvalidResult) {
                return INVALID_RESULT;
            }
            else {
                return NOT_VALIDATED_RESULT;
            }
        }

        Set<String> groups = new HashSet<>();

        // Take the groups from the identity store that validated the credentials only
        // if it has been set to provide groups.
        if (identityStore.validationTypes().contains(PROVIDE_GROUPS)) {
            groups.addAll(validationResult.getCallerGroups());
        }

        // Ask all stores that were configured for group providing only to get the groups for the
        // authenticated caller
        CredentialValidationResult finalResult = validationResult; // compiler didn't like validationResult in the enclosed scope
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                for (IdentityStore authorizationIdentityStore : authorizationIdentityStores) {
                    groups.addAll(authorizationIdentityStore.getCallerGroups(finalResult));
                }
                return null;
            }
        });

        return new CredentialValidationResult(
                validationResult.getIdentityStoreId(),
                validationResult.getCallerPrincipal(),
                validationResult.getCallerDn(),
                validationResult.getCallerUniqueId(),
                groups);
    }

}
