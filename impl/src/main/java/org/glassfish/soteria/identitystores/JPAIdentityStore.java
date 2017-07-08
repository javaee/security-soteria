/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved.
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
import static java.util.Collections.unmodifiableSet;
import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.enterprise.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import org.glassfish.soteria.identitystores.annotation.JPAIdentityStoreDefinition;

public class JPAIdentityStore implements IdentityStore {

    private final JPAIdentityStoreDefinition jpaIdentityStoreDefinition;

    private final Set<ValidationType> validationTypes;

    public JPAIdentityStore(JPAIdentityStoreDefinition jpaIdentityStoreDefinition) {
        this.jpaIdentityStoreDefinition = jpaIdentityStoreDefinition;
        validationTypes = unmodifiableSet(new HashSet<>(asList(jpaIdentityStoreDefinition.useFor())));
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            return validate((UsernamePasswordCredential) credential);
        }

        return NOT_VALIDATED_RESULT;
    }

    public CredentialValidationResult validate(UsernamePasswordCredential usernamePasswordCredential) {
        CredentialValidationResult result = INVALID_RESULT;

        EntityManager em = Persistence.createEntityManagerFactory(jpaIdentityStoreDefinition.persistenceUnitName()).
                createEntityManager();

        String password = null;
        try {
            password = (String) em.createQuery(jpaIdentityStoreDefinition.callerQuery()).
                    setParameter(1, usernamePasswordCredential.getCaller()).
                    getSingleResult();
        } catch (NoResultException e) {
            // Do nothing 
        }

        // TODO Support for hashed passwords.
        if (usernamePasswordCredential.getPassword().compareTo(password)) {
            List<String> groups = (List<String>) em.createQuery(jpaIdentityStoreDefinition.groupsQuery()).
                    setParameter(1, usernamePasswordCredential.getCaller()).
                    getResultList();

            result = new CredentialValidationResult(new CallerPrincipal(usernamePasswordCredential.getCaller()), new HashSet<>(groups));
        }

        em.close();

        return result;
    }

    @Override
    public Set<String> getCallerGroups(CredentialValidationResult validationResult) {
        EntityManager em = Persistence.createEntityManagerFactory(jpaIdentityStoreDefinition.persistenceUnitName()).
                createEntityManager();

        List<String> groups = (List<String>) em.createQuery(jpaIdentityStoreDefinition.groupsQuery()).
                setParameter(1, validationResult.getCallerPrincipal().getName()).
                getResultList();

        em.close();

        return new HashSet<>(groups);
    }

    @Override
    public int priority() {
        return jpaIdentityStoreDefinition.priority();
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return validationTypes;
    }
}
