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
package org.glassfish.soteria.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class Assert {
    
    public static void assertDefaultAuthenticated(String response) {
<<<<<<< HEAD
        assertAuthenticated("web", "reza", response, "foo", "bar");
    }
    
    public static void assertDefaultNotAuthenticated(String response) {
        assertNotAuthenticated("web", "reza", response, "foo", "bar");
    }
    
    public static void assertAuthenticated(String userType, String name, String response, String... roles) {
=======
        assertAuthenticated("web", "reza", response);
    }
    
    public static void assertAuthenticated(String userType, String name, String response) {
>>>>>>> upstream/master
        assertTrue(
            "Should be authenticated as user " + name + " but was not \n Response: \n" + 
            response + "\n search: " + userType + " username: " + name,
            response.contains(userType + " username: " + name));
<<<<<<< HEAD
        
        for (String role : roles) {
            assertTrue(
                "Authenticated user should have role \"" + role + "\", but did not \n Response: \n" + 
                response,
                response.contains(userType + " user has role \"" + role + "\": true"));
        }
    }
    
    public static void assertNotAuthenticated(String userType, String name, String response, String... roles) {
        assertFalse(
            "Should not be authenticated as user " + name + " but was \n Response: \n" + 
            response + "\n search: " + userType + " username: " + name,
            response.contains(userType + " username: " + name));
        
        for (String role : roles) {
            assertFalse(
                "Authenticated user should not have role \"" + role + "\", but did \n Response: \n" + 
                response,
                response.contains(userType + " user has role \"" + role + "\": true"));
        }
=======
        assertTrue(
            "Authenticated user should have role \"foo\", but did not \n Response: \n" + 
            response,
            response.contains(userType + " user has role \"foo\": true"));
        assertTrue(
            "Authenticated user should have role \"bar\", but did not \n Response: \n" + 
            response,
            response.contains(userType + " user has role \"bar\": true"));
    }
     
    public static void assertDefaultNotAuthenticated(String response) {
        assertFalse(
            "Should not be authenticated as user reza but was \n Response: \n" + 
            response,
            response.contains("web username: reza"));
        assertFalse(
            "Authenticated user should not have role \"foo\", but did \n Response: \n" + 
            response,
            response.contains("web user has role \"foo\": true"));
        assertFalse(
            "Authenticated user should not have role \"bar\", but did \n Response: \n" + 
            response,
            response.contains("web user has role \"bar\": true"));
     }
    
    public static void assertNotAuthenticated(String userType, String response) {
        assertFalse(
            "Should not be authenticated as user reza but was \n Response: \n" + 
            response,
            response.contains(userType + " username: reza"));
        assertFalse(
            "Authenticated user should not have role \"foo\", but did \n Response: \n" + 
            response,
            response.contains(userType + " user has role \"foo\": true"));
        assertFalse(
            "Authenticated user should not have role \"bar\", but did \n Response: \n" + 
            response,
            response.contains(userType + " user has role \"bar\": true"));
>>>>>>> upstream/master
     }

}
