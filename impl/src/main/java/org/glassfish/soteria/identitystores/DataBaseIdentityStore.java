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
package org.glassfish.soteria.identitystores;

import javax.security.CallerPrincipal;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.annotation.DataBaseIdentityStoreDefinition;
import javax.security.identitystore.credential.Credential;
import javax.security.identitystore.credential.UsernamePasswordCredential;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static javax.security.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;
import static org.glassfish.soteria.cdi.CdiUtils.jndiLookup;

public class DataBaseIdentityStore implements IdentityStore {

    private DataBaseIdentityStoreDefinition dataBaseIdentityStoreDefinition;

    private ValidationType validationType;

    public DataBaseIdentityStore(DataBaseIdentityStoreDefinition dataBaseIdentityStoreDefinition) {
        this.dataBaseIdentityStoreDefinition = dataBaseIdentityStoreDefinition;
        determineValidationType();
    }

    private void determineValidationType() {
        validationType = ValidationType.BOTH;
        if (dataBaseIdentityStoreDefinition.authenticateOnly()) {
            validationType = ValidationType.AUTHENTICATION;
        } else {
            if (dataBaseIdentityStoreDefinition.authorizeOnly()) {
                validationType = ValidationType.AUTHORIZATION;
            }
        }
    }

    @Override
    public CredentialValidationResult validate(Credential credential, CallerPrincipal callerPrincipal) {
        if (credential instanceof UsernamePasswordCredential) {
            return validate((UsernamePasswordCredential) credential, callerPrincipal);
        }

        return NOT_VALIDATED_RESULT;
    }

    public CredentialValidationResult validate(UsernamePasswordCredential usernamePasswordCredential, CallerPrincipal callerPrincipal) {

        DataSource dataSource = jndiLookup(dataBaseIdentityStoreDefinition.dataSourceLookup());

        boolean authenticated = true;
        String caller = null;
        if (validationType == ValidationType.AUTHENTICATION || validationType == ValidationType.BOTH) {
            List<String> passwords = executeQuery(
                    dataSource,
                    dataBaseIdentityStoreDefinition.callerQuery(),
                    usernamePasswordCredential.getCaller()
            );
            // TODO Support for hashed passwords.
            authenticated = (!passwords.isEmpty() && usernamePasswordCredential.getPassword().compareTo(passwords.get(0)));

            if (authenticated) {
                caller = usernamePasswordCredential.getCaller();
            }
        } else {
            // We are Authorize Only mode, so get the caller determined previously.
            if (callerPrincipal != null) {
                caller = callerPrincipal.getName();
            }
            // When callerPrincipal is empty means the authentication failed and caller remains null.
        }

        // We check also if caller != null to be sure the Authentication by another IdentityStore succeeded.
        if (authenticated && caller != null) {
            if (validationType == ValidationType.AUTHORIZATION || validationType == ValidationType.BOTH) {

                return new CredentialValidationResult(
                        caller,
                        executeQuery(
                                dataSource,
                                dataBaseIdentityStoreDefinition.groupsQuery(),
                                caller
                        )
                );
            } else {
                // Authentication only
                return new CredentialValidationResult(caller);

            }
        }

        return INVALID_RESULT;
    }

    private List<String> executeQuery(DataSource dataSource, String query, String parameter) {
        List<String> result = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, parameter);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(resultSet.getString(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }

    public int priority() {
        return dataBaseIdentityStoreDefinition.priority();
    }

    @Override
    public ValidationType validationType() {
        return validationType;
    }
}
