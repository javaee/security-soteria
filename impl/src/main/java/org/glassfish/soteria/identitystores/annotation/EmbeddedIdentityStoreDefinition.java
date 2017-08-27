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
 * Annotation used to define a container provided {@link IdentityStore} that stores
 * caller credentials and identity attributes (together caller identities) in an 
 * in-memory store, and make that implementation available as an enabled CDI bean.
 * 
 * <p>
 * The data in this store is set at definition time only via the {@link #value()} attribute
 * of this annotation.
 * 
 * <p>
 * The following shows an example:
 * 
 * <pre>
 * <code>
 * {@literal @}EmbeddedIdentityStoreDefinition({ 
 *  {@literal @}Credentials(callerName = "peter", password = "secret1", groups = { "foo", "bar" }),
 *  {@literal @}Credentials(callerName = "john", password = "secret2", groups = { "foo", "kaz" }),
 *  {@literal @}Credentials(callerName = "carla", password = "secret3", groups = { "foo" }) })
 * </code>
 * </pre>
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface EmbeddedIdentityStoreDefinition {

    /**
     * Defines the caller identities stored in the embedded identity store
     * 
     * @return caller identities stored in the embedded identity store
     */
    Credentials[] value() default {};

    /**
     * Determines the order in case multiple IdentityStores are found.
     * @return the priority.
     */
    int priority() default 90;

    /**
     * Determines what the identity store is used for
     * 
     * @return the type the identity store is used for
     */
    ValidationType[] useFor() default {VALIDATE, PROVIDE_GROUPS};

}
