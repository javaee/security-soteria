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
package javax.security.identitystore.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.security.identitystore.IdentityStore;

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
 * @author Arjan Tijms
 *
 * TODO: EDR1 concern: terminology "caller credentials", "identity attributes", "caller identity"
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
     * Defines if the IdentityStore does only Authentication. By default this value is false so that authentication and authorization are performed.
     * @return does the IdentityStore only do Authentication?
     */
    boolean authenticateOnly() default false;

    /**
     * Defines if the IdentityStore does only Authorization. By default this value is false so that authentication and authorization are performed.
     * @return does the IdentityStore only do Authentication?
     */
    boolean authorizeOnly() default false;

}