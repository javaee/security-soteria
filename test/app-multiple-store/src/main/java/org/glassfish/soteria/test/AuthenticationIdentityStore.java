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
package org.glassfish.soteria.test;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.security.CallerPrincipal;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.credential.Credential;
import javax.security.identitystore.credential.UsernamePasswordCredential;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@RequestScoped
public class AuthenticationIdentityStore implements IdentityStore {

    private Map<String, String> callerToPassword;

    @PostConstruct
    public void init() {
        callerToPassword = new HashMap<>();
        callerToPassword.put("reza", "secret1");
        callerToPassword.put("alex", "secret2");
        callerToPassword.put("arjan", "secret3");
    }

    @Override
    public CredentialValidationResult validate(CredentialValidationResult partialValidationResult, Credential credential) {

        CredentialValidationResult result = partialValidationResult;
        if (partialValidationResult.getStatus() != CredentialValidationResult.Status.AUTHENTICATED &&
                partialValidationResult.getStatus() != CredentialValidationResult.Status.VALID) {

            if (credential instanceof UsernamePasswordCredential) {
                UsernamePasswordCredential usernamePassword = (UsernamePasswordCredential) credential;
                String expectedPW = callerToPassword.get(usernamePassword.getCaller());
                // We don't allow empty passwords :)
                if (expectedPW != null && expectedPW.equals(usernamePassword.getPasswordAsString())) {
                    result = new CredentialValidationResult(partialValidationResult, CredentialValidationResult.Status.AUTHENTICATED
                            , new CallerPrincipal(usernamePassword.getCaller()), new ArrayList<>());
                } else {
                    result = CredentialValidationResult.INVALID_RESULT;
                }
            } else {
                result = CredentialValidationResult.NOT_VALIDATED_RESULT;
            }
        }

        return result;
    }

    @Override
    public int priority() {
        return 5;
    }
}
