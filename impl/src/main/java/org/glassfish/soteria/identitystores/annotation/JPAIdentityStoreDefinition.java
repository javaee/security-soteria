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
package org.glassfish.soteria.identitystores.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.VALIDATE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.IdentityStore.ValidationType;

/**
 *
 * @author Guillermo González de Agüero
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface JPAIdentityStoreDefinition {

    /**
     * Name of the persistence unit name on which queries will be performed.
     *
     * Optional: when there's only one container managed persistence unit, that
     * one will be used
     *
     * @return persistence unit name
     */
    String persistenceUnitName() default "";

    /**
     * JPQL query to validate the {caller, password} pair.
     *
     * Only needed when {@link #useFor()} contains
     * {@link ValidationType#VALIDATE}.
     *
     * <p>
     * The name of the caller that is to be authenticated has to be set as the
     * one and only placeholder. The (hashed) password should be in the first
     * column of the result.
     *
     * <p>
     * Example query:
     * <pre>
     * <code>
     * SELECT c.password FROM Caller c WHERE c.name = ?1
     * </code>
     * </pre>
     *
     * @return JPQL query to validate
     */
    String callerQuery() default "";

    /**
     * JPQL query to retrieve the groups associated with the caller when
     * authentication succeeds.
     *
     * Only needed when {@link #useFor()} contains
     * {@link ValidationType#PROVIDE_GROUPS}.
     *
     * <p>
     * The name of the caller that has been authenticated has to be set as the
     * one and only placeholder. The group name should be in the first column of
     * the result.
     *
     * <p>
     * Example query:
     * <pre>
     * <code>
     * SELECT g.name FROM Group g JOIN g.callers c WHERE c.name = ?1
     * </code>
     * </pre>
     *
     * @return JPQL query to retrieve the groups
     */
    String groupsQuery() default "";

    /**
     * Hash algorithm applied to plain text password for comparison with
     * password returned from {@link #groupsQuery()}.
     *
     * @return Hash algorithm applied to plain text password
     */
    String hashAlgorithm() default ""; // default no hash (for now) todo: make enum?

    /**
     * Encoding used for hash. TODO
     *
     * @return Encoding used for hash
     */
    String hashEncoding() default ""; // default no encoding (for now) todo: make enum?

    /**
     * Determines the order in case multiple IdentityStores are found.
     *
     * @return the priority.
     */
    int priority() default 70;

    /**
     * Determines what the identity store is used for
     *
     * @return the type the identity store is used for
     */
    ValidationType[] useFor() default {VALIDATE, PROVIDE_GROUPS};

}
