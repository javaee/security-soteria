/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package javax.security.identitystore.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.security.identitystore.IdentityStore;

/**
 * Annotation used to define a container provided {@link IdentityStore} that stores
 * caller credentials and identity attributes (together caller identities) in an 
 * LDAP store, and make that implementation available as an enabled CDI bean.
 * 
 * @author Arjan Tijms
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface LdapIdentityStoreDefinition {

    /**
     * URL where the LDAP server can be reached.
     * E.g. <code>ldap://localhost:33389"</code>
     * 
     * @return URL where the LDAP server can be reached
     */
    String url() default "";
    
    /**
     * Base of the distinguished name that contains the caller name.
     * E.g. <code>ou=caller,dc=jsr375,dc=net</code>
     * 
     * @return Base of the distinguished name that contains the caller name
     */
    String callerBaseDn() default "";
    
    /**
     * Name of the attribute that contains the caller name in the node
     * just below the one identified by {@link #callerBaseDn()}.
     * E.g. <code>uid</code>
     * <p>
     * Example for the relationship with {@link #callerBaseDn()} and the name
     * of the caller that needs to be authenticated:
     * <br>
     * Given the DN <code>uid=peter,ou=caller,dc=jsr375,dc=net</code>,
     * <ul>
     * <li> {@link #callerNameAttribute()} corresponds to <code>uid</code> </li>
     * <li> {@link #callerBaseDn()} corresponds to <code>ou=caller,dc=jsr375,dc=net</code> </li>
     * <li> <code>peter</code> is the caller name that needs to be authenticated </li>
     * </ul>
     * <p>
     * The following gives an example in ldif format:
     * <pre>
     * <code>
     * dn: uid=peter,ou=caller,dc=jsr375,dc=net
     * objectclass: top
     * objectclass: uidObject
     * objectclass: person
     * uid: peter
     * cn: Peter Smith
     * sn: Peter
     * userPassword: secret1
     * </code>
     * </pre>
     * 
     * @return Name of the attribute that contains the caller name
     */
    String callerNameAttribute() default "uid";
    
    /**
     * Base of the distinguished name that contains the groups
     * E.g. <code>ou=group,dc=jsr375,dc=net</code>
     * 
     * @return Base of the distinguished name that contains the groups
     */
	String groupBaseDn() default "";
	
	/**
     * Name of the attribute that contains the group name in the node
     * just below the one identified by {@link #groupBaseDn()}.
     * E.g. <code>cn</code>
     * <p>
     * Example for the relationship with {@link #groupBaseDn()} and the role name
     * <br>
     * Given the DN <code>cn=foo,ou=group,dc=jsr375,dc=net</code>,
     * <ul>
     * <li> {@link #groupNameAttribute()} corresponds to <code>cn</code> </li>
     * <li> {@link #groupBaseDn()} corresponds to <code>ou=group,dc=jsr375,dc=net</code> </li>
     * <li> <code>foo</code> is the group name that will be returned by the store when authentication succeeds</li>
     * </ul>
     * 
     * @return Name of the attribute that contains the group name
	 */
	String groupNameAttribute() default "cn";
	
	/**
	 * DN attribute for the group DN that identifies the callers that are in that group.
	 * E.g. <code>member</code>
     * <p>
     * The value of this attribute has to the full DN of the caller. The following gives an example
     * entry in ldif format:
     * <pre>
     * <code>
     * dn: cn=foo,ou=group,dc=jsr375,dc=net
     * objectclass: top
     * objectclass: groupOfNames
     * cn: foo
     * member: uid=pete,ou=caller,dc=jsr375,dc=net
     * member: uid=john,ou=caller,dc=jsr375,dc=net 
     * </code>
     * </pre>
     * 
     * @return DN attribute for the group DN
	 */
	String groupCallerDnAttribute() default "member";

}