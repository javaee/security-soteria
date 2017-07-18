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
package org.glassfish.soteria.identitystores;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Function.identity;
import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.enterprise.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;
import static org.glassfish.soteria.cdi.AnnotationELPProcessor.hasAnyELExpression;
import static org.glassfish.soteria.cdi.CdiUtils.jndiLookup;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.sql.DataSource;

import org.glassfish.soteria.cdi.CdiUtils;

public class DatabaseIdentityStore implements IdentityStore {

    private final DatabaseIdentityStoreDefinition dataBaseIdentityStoreDefinition;

    private final Set<ValidationType> validationTypes;
    private final Function<String, String> hashFunction;
    
    private final byte[] salt = new byte[16]; // TODO

    public DatabaseIdentityStore(DatabaseIdentityStoreDefinition dataBaseIdentityStoreDefinition) {
        this.dataBaseIdentityStoreDefinition = dataBaseIdentityStoreDefinition;
        validationTypes = unmodifiableSet(new HashSet<>(asList(dataBaseIdentityStoreDefinition.useFor())));
        
        if (hasAnyELExpression(dataBaseIdentityStoreDefinition.hashAlgorithm())) {
            ELContext elContext = CdiUtils.getELProcessor().getELManager().getELContext();
            
            MethodExpression hashMethodExpression = ExpressionFactory.newInstance().createMethodExpression(
                elContext, 
                dataBaseIdentityStoreDefinition.hashAlgorithm(), 
                String.class, new  Class<?>[] {String.class} );
            
            hashFunction =  s -> (String) hashMethodExpression.invoke(elContext, new Object[] {s});
        } else if ("PBKDF2".equals(dataBaseIdentityStoreDefinition.hashAlgorithm())) {
            hashFunction = s -> pbkdf2(s, salt);
        } else {
            hashFunction = identity();
        }
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            return validate((UsernamePasswordCredential) credential);
        }

        return NOT_VALIDATED_RESULT;
    }

    public CredentialValidationResult validate(UsernamePasswordCredential usernamePasswordCredential) {

        DataSource dataSource = jndiLookup(dataBaseIdentityStoreDefinition.dataSourceLookup());
        
        List<String> passwords = executeQuery(
            dataSource, 
            dataBaseIdentityStoreDefinition.callerQuery(),
            usernamePasswordCredential.getCaller()
        ); 

        String hashedPassword = hashFunction.apply(usernamePasswordCredential.getPasswordAsString());
        
        if (!passwords.isEmpty() && hashedPassword.equals(passwords.get(0))) {
            Set<String> groups = emptySet();

            if (validationTypes.contains(ValidationType.PROVIDE_GROUPS)) {
                groups = new HashSet<>(executeQuery(dataSource, dataBaseIdentityStoreDefinition.groupsQuery(), usernamePasswordCredential.getCaller()));
            }

            return new CredentialValidationResult(new CallerPrincipal(usernamePasswordCredential.getCaller()), groups);
        }

        return INVALID_RESULT;
    }
    
    @Override
    public Set<String> getCallerGroups(CredentialValidationResult validationResult) {
        
        DataSource dataSource = jndiLookup(dataBaseIdentityStoreDefinition.dataSourceLookup());
        
        return new HashSet<>(executeQuery(
            dataSource,
            dataBaseIdentityStoreDefinition.groupsQuery(),
            validationResult.getCallerPrincipal().getName())
        );
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

    @Override
    public int priority() {
        return dataBaseIdentityStoreDefinition.priority();
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return validationTypes;
    }
    
    public String pbkdf2(String password, byte[] salt) {
        try {
            return 
                Base64.getEncoder()
                      .encodeToString(
                          SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                                          .generateSecret(
                                             new PBEKeySpec(password.toCharArray(), salt, 1024, 64 * 8))
                                          .getEncoded());
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }
}
