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

import static org.glassfish.soteria.test.ShrinkWrap.mavenWar;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class AppJaxRsIT extends ArquillianBase {
    
    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return mavenWar();
    }

    @Test
    public void testAuthenticated() {
        String response = readFromServer("/rest/resource/callerName?name=reza&password=secret1");
        
        assertTrue(
            "Should be authenticated as user reza but was not",
            response.contains("reza"));
    }
    
    @Test
    public void testNotAuthenticated() {
        String response = readFromServer("/rest/resource/callerName");
        
        assertFalse(
            "Should not be authenticated as user reza but was",
            response.contains("reza"));
    }
    
    @Test
    public void testHasRoleFoo() {
        String response = readFromServer("/rest/resource/hasRoleFoo?name=reza&password=secret1");
        
        assertTrue(
            "Should be in role foo, but was not",
            response.contains("true"));
    }
    
    @Test
    public void testNotHasRoleFoo() {
        String response = readFromServer("/rest/resource/hasRoleFoo");
        
        assertTrue(
            "Should not be in role foo, but was",
            response.contains("false"));
    }
    
    @Test
    public void testNotHasRoleKaz1() {
        String response = readFromServer("/rest/resource/hasRoleKaz?name=reza&password=secret1");
        
        assertFalse(
            "Should not be in role kaz, but was",
            response.contains("true"));
    }
    
    @Test
    public void testNotHasRoleKaz2() {
        String response = readFromServer("/rest/resource/hasRoleKaz");
        
        assertFalse(
            "Should not be in role kaz, but was",
            response.contains("true"));
    }
    
    @Test
    public void testSayHi() {
        String response = readFromServer("/rest/protectedResource/sayHi?name=reza&password=secret1");
        
        assertTrue(
            "Endpoint should have been called, but was not",
            response.contains("saying hi!"));
    }
    
    @Test
    public void testNotSayHi() {
        String response = readFromServer("/rest/protectedResource/sayHi");
        
        assertFalse(
            "Endpoint should not have been called, but was",
            response.contains("saying hi!"));
    }

}
