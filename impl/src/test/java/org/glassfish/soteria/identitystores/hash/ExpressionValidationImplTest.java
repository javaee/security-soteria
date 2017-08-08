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
package org.glassfish.soteria.identitystores.hash;

import java.lang.annotation.Annotation;
import javax.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import org.junit.Before;
import org.junit.Test;

import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.IdentityStore.ValidationType;
import javax.security.enterprise.identitystore.LdapIdentityStoreDefinition;
import javax.security.enterprise.identitystore.LdapIdentityStoreDefinition.LdapSearchScope;
import javax.security.enterprise.identitystore.PasswordHash;

import org.glassfish.soteria.cdi.DatabaseIdentityStoreDefinitionAnnotationLiteral;
import org.glassfish.soteria.cdi.LdapIdentityStoreDefinitionAnnotationLiteral;

public class ExpressionValidationImplTest {

    private DatabaseIdentityStoreDefinitionAnnotationLiteral validDatabase;
    private DatabaseIdentityStoreDefinitionAnnotationLiteral invalidDatabasePriority;
    private DatabaseIdentityStoreDefinitionAnnotationLiteral invalidDatabaseUseFor;

    private LdapIdentityStoreDefinitionAnnotationLiteral validLdap;
    private LdapIdentityStoreDefinitionAnnotationLiteral invalidLdapCallerSearchScope;
    private LdapIdentityStoreDefinitionAnnotationLiteral invalidLdapGroupSearchScope;
    private LdapIdentityStoreDefinitionAnnotationLiteral invalidLdapMaxResultsExpression;
    private LdapIdentityStoreDefinitionAnnotationLiteral invalidLdapPriorityExpression;
    private LdapIdentityStoreDefinitionAnnotationLiteral invalidLdapReadTimeout;
    private LdapIdentityStoreDefinitionAnnotationLiteral invalidLdapUseForExpression;

    @Before
    public void init() {
        validDatabase = createDatabaseIdentityStoreDefinition("#{foo.bar}", "#{foo.bar}");
        invalidDatabasePriority = createDatabaseIdentityStoreDefinition("xx", "#{foo.bar}");
        invalidDatabaseUseFor = createDatabaseIdentityStoreDefinition("#{foo.bar}", "xx");

        validLdap = createLdapIdentityStoreDefinition("#{foo.bar}", "#{foo.bar}", "#{foo.bar}", "#{foo.bar}", "#{foo.bar}", "#{foo.bar}");
        invalidLdapCallerSearchScope = createLdapIdentityStoreDefinition("xx", "#{foo.bar}", "#{foo.bar}", "#{foo.bar}", "#{foo.bar}", "#{foo.bar}");
        invalidLdapGroupSearchScope = createLdapIdentityStoreDefinition("#{foo.bar}", "xx", "#{foo.bar}", "#{foo.bar}", "#{foo.bar}", "#{foo.bar}");
        invalidLdapMaxResultsExpression = createLdapIdentityStoreDefinition("#{foo.bar}", "#{foo.bar}", "xx", "#{foo.bar}", "#{foo.bar}", "#{foo.bar}");
        invalidLdapPriorityExpression = createLdapIdentityStoreDefinition("#{foo.bar}", "#{foo.bar}", "#{foo.bar}", "xx", "#{foo.bar}", "#{foo.bar}");
        invalidLdapReadTimeout = createLdapIdentityStoreDefinition("#{foo.bar}", "#{foo.bar}", "#{foo.bar}", "#{foo.bar}", "xx", "#{foo.bar}");
        invalidLdapUseForExpression = createLdapIdentityStoreDefinition("#{foo.bar}", "#{foo.bar}", "#{foo.bar}", "#{foo.bar}", "#{foo.bar}", "xx");
    }

    @Test
    public void testValidDatabase() {
        DatabaseIdentityStoreDefinitionAnnotationLiteral.eval(validDatabase);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailsDatabasePriority() {
        DatabaseIdentityStoreDefinitionAnnotationLiteral.eval(invalidDatabasePriority);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailsDatabaseUseFor() {
        DatabaseIdentityStoreDefinitionAnnotationLiteral.eval(invalidDatabaseUseFor);
    }

    @Test
    public void testValidLdap() {
        LdapIdentityStoreDefinitionAnnotationLiteral.eval(validLdap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailsLdapCallerSearchScope() {
        LdapIdentityStoreDefinitionAnnotationLiteral.eval(invalidLdapCallerSearchScope);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailsLdapGroupSearchScope() {
        LdapIdentityStoreDefinitionAnnotationLiteral.eval(invalidLdapGroupSearchScope);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailsLdapMaxResults() {
        LdapIdentityStoreDefinitionAnnotationLiteral.eval(invalidLdapMaxResultsExpression);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailsLdapPriority() {
        LdapIdentityStoreDefinitionAnnotationLiteral.eval(invalidLdapPriorityExpression);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailsLdapReadTimeout() {
        LdapIdentityStoreDefinitionAnnotationLiteral.eval(invalidLdapReadTimeout);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailsLdapUseFor() {
        LdapIdentityStoreDefinitionAnnotationLiteral.eval(invalidLdapUseForExpression);
    }

    private DatabaseIdentityStoreDefinitionAnnotationLiteral createDatabaseIdentityStoreDefinition(String priorityExpression, String useForExpression) {
        return new DatabaseIdentityStoreDefinitionAnnotationLiteral(
                "java:comp/DefaultDataSource", "", "", Pbkdf2PasswordHashImpl.class, new String[]{},
                0, priorityExpression, IdentityStore.ValidationType.values(), useForExpression);
    }

    private LdapIdentityStoreDefinitionAnnotationLiteral createLdapIdentityStoreDefinition(
            String callerSearchScopeExpression, String groupSearchScopeExpression, String maxResultsExpression, String priorityExpression, String readTimeoutExpression,
            String useForExpression) {
        return new LdapIdentityStoreDefinitionAnnotationLiteral(
                "", "", "", "uid", "", "", LdapIdentityStoreDefinition.LdapSearchScope.SUBTREE, callerSearchScopeExpression, "member", "memberOf",
                "cn", "", "", LdapIdentityStoreDefinition.LdapSearchScope.SUBTREE, groupSearchScopeExpression, 1000, maxResultsExpression, 80, priorityExpression, 0,
                readTimeoutExpression, "", IdentityStore.ValidationType.values(), useForExpression);
    }
}
