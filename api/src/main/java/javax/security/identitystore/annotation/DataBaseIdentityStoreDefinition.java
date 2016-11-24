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
 * caller credentials and identity attributes in a relational database, and make that 
 * implementation available as an enabled CDI bean.
 * 
 * @author Arjan Tijms
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface DataBaseIdentityStoreDefinition {

    /**
     * Full JNDI name of the data source that provides access to the data base where the 
     * caller identities are stored. 
     * 
     * @return Full JNDI name of the data source
     */
	String dataSourceLookup() default "java:comp/DefaultDataSource"; // default data source
	
	/**
	 * SQL query to validate the {caller, password} pair. 
	 * 
	 * <p>
	 * The name of the caller that is to be authenticated has to be set as the one and only placeholder. 
	 * The (hashed) password should be in the first column of the result.
	 * 
	 * <p>
	 * Example query:
	 * <pre>
	 * <code>
	 * select password from caller where name = ?
	 * </code>
	 * </pre>
	 * 
	 * @return SQL query to validate
	 */
	String callerQuery();
	
	/**
     * SQL query to retrieve the groups associated with the caller when authentication succeeds.
     * 
     * <p>
     * The name of the caller that has been authenticated has to be set as the one and only placeholder. 
     * The group name should be in the first column of the result.
     * 
     * <p>
     * Example query:
     * <pre>
     * <code>
     * select group_name from caller_groups where caller_name = ?
     * </code>
     * </pre>
     * 
     * @return SQL query to retrieve the groups
     */
	String groupsQuery();
	
	/**
	 * Hash algorithm applied to plain text password for comparison with password
	 * returned from {@link #groupsQuery()}.
	 * 
	 * @return Hash algorithm applied to plain text password
	 */
	String hashAlgorithm() default ""; // default no hash (for now) todo: make enum?
	
	/**
	 * Encoding used for hash. TODO
	 * 
	 *  @return Encoding used for hash
	 */
	String hashEncoding() default ""; // default no encoding (for now) todo: make enum?

	/**
	 * Determines the order in case multiple IdentityStores are found.
	 * @return the priority.
	 */
	int priority() default 70;

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