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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebResponse;


@RunWith(Arquillian.class)
public class AppMemBasicIT extends ArquillianBase {
    
    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return mavenWar();
    }

    @Test
    public void testAuthenticated() {
    	
    	DefaultCredentialsProvider credentialsProvider = new DefaultCredentialsProvider();
    	credentialsProvider.addCredentials("reza", "secret1");
    	
    	getWebClient().setCredentialsProvider(credentialsProvider);
    	
        assertDefaultAuthenticated(
            readFromServer("/servlet"));
    }
    
    @Test
    public void testNotAuthenticated() {
        
        WebResponse response = responseFromServer("/servlet");
        
        assertEquals(401, response.getStatusCode());
        
        assertTrue(
            "Response did not contain the \"WWW-Authenticate\" header, but should have", 
            response.getResponseHeaderValue("WWW-Authenticate") != null);
        
        assertDefaultNotAuthenticated(
            response.getContentAsString());
    }
    
    @Test
    public void testNotAuthenticatedWrongName() {
    	
    	DefaultCredentialsProvider credentialsProvider = new DefaultCredentialsProvider();
    	credentialsProvider.addCredentials("romo", "secret1");
    	
    	getWebClient().setCredentialsProvider(credentialsProvider);
    	
    	WebResponse response = responseFromServer("/servlet");
          
    	assertEquals(401, response.getStatusCode());
          
    	assertTrue(
	        "Response did not contain the \"WWW-Authenticate\" header, but should have", 
	        response.getResponseHeaderValue("WWW-Authenticate") != null);
          
    	assertDefaultNotAuthenticated(
	        response.getContentAsString());
    }
    
    @Test
    public void testNotAuthenticatedWrongPassword() {
    	
      	DefaultCredentialsProvider credentialsProvider = new DefaultCredentialsProvider();
    	credentialsProvider.addCredentials("reza", "wrongpassword");
    	
    	getWebClient().setCredentialsProvider(credentialsProvider);
    	
        WebResponse response = responseFromServer("/servlet");
        
        assertEquals(401, response.getStatusCode());
          
        assertTrue(
            "Response did not contain the \"WWW-Authenticate\" header, but should have", 
            response.getResponseHeaderValue("WWW-Authenticate") != null);
          
        assertDefaultNotAuthenticated(
            response.getContentAsString());
    }

}
