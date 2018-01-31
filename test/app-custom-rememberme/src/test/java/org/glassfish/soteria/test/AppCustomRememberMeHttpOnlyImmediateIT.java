/*
 * Copyright (c) 2015-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.glassfish.soteria.test;

import static org.glassfish.soteria.test.Assert.assertDefaultAuthenticated;
import static org.glassfish.soteria.test.Assert.assertDefaultNotAuthenticated;
import static org.glassfish.soteria.test.ShrinkWrap.mavenWar;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.glassfish.soteria.test.alternatives.TestAuthenticationMechanismHttpOnlyFalseImmediate;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.util.Cookie;


@RunWith(Arquillian.class)
public class AppCustomRememberMeHttpOnlyImmediateIT extends ArquillianBase {
    
    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return mavenWar()
                .addClass(TestAuthenticationMechanismHttpOnlyFalseImmediate.class);
    }
    
    @Test
    public void testHttpOnlyIsFalse() {
        readFromServer("/servlet?name=reza&password=secret1&rememberme=true");
        
        assertFalse(getWebClient().getCookieManager().getCookie("JREMEMBERMEID").isHttpOnly());
    }

    @Test
    public void testAuthenticated() {
        assertDefaultAuthenticated(
            readFromServer("/servlet?name=reza&password=secret1"));
    }
    
    @Test
    public void testNotAuthenticated() {
        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));
    }
    
    @Test
    public void testNotAuthenticatedWrongName() {
        assertDefaultNotAuthenticated(
            readFromServer("/servlet?name=romo&password=secret1"));
    }
    
    @Test
    public void testNotAuthenticatedWrongPassword() {
        assertDefaultNotAuthenticated(
            readFromServer("/servlet?name=reza&password=wrongpassword"));
    }
    
    @Test
    public void testAuthenticatedRememberMe() {
        
        // 1. Initially request page when we're not authenticated
        
        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));
        
        
        // 2. Authenticate without remember me
        
        String response = readFromServer("/servlet?name=reza&password=secret1");
        
        assertDefaultAuthenticated(
            response);
        
        // For the initial authentication, the mechanism should be called
        
        assertTrue(
            "Authentication mechanism should have been called, but wasn't", 
            response.contains("authentication mechanism called: true"));
        
        
        // 3. Request same page again within same http session, without remember me
        //    specified should NOT be authenticated
        
        response = readFromServer("/servlet");
        
        assertDefaultNotAuthenticated(
            response);
        
   
        // 4. Authenticate with remember me
        
        response = readFromServer("/servlet?name=reza&password=secret1&rememberme=true");
        
        assertDefaultAuthenticated(
            response);
        
        // For the initial authentication, the mechanism should be called again
        
        assertTrue(
            "Authentication mechanism should have been called, but wasn't", 
            response.contains("authentication mechanism called: true"));
        
        
        // 5. Request same page again within same http session, with remember me
        //    specified should be authenticated
        
        response = readFromServer("/servlet");
        
        assertDefaultAuthenticated(
            response);
        
        // For the subsequent authentication, the mechanism should not be called again
        // (the remember me interceptor takes care of this)
        
        assertTrue(
            "Authentication mechanism should not have been called, but was", 
            response.contains("authentication mechanism called: false"));
        
        
        // 6. "Expire" the session by removing all cookies except the
        //    remember me cookie
        
        for (Cookie cookie : getWebClient().getCookieManager().getCookies()) {
            if (!"JREMEMBERMEID".equals(cookie.getName())) {
                getWebClient().getCookieManager().removeCookie(cookie);
            }
        }
        
        // Request same page again
        
        response = readFromServer("/servlet");
        
        // Should still be authenticated
        
        assertDefaultAuthenticated(
            response);
        
        // For the subsequent authentication, the mechanism should not be called again
        // (the remember me interceptor takes care of this)
        
        assertTrue(
            "Authentication mechanism should not have been called, but was", 
            response.contains("authentication mechanism called: false"));

        
        // 7. Logout. Should not be authenticated anymore
        
        System.out.println("\nLogging out\n");
        
        assertDefaultNotAuthenticated(
            readFromServer("/servlet?logout=true"));
        
        
        // 8. Request same page again, should still not be authenticated
        
        assertDefaultNotAuthenticated(
            readFromServer("/servlet"));
       
    }

    
   

}
