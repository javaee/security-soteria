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
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class AppSecurityContextCallerPrincipalIT extends ArquillianBase {

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return mavenWar();
    }

    @Test
    public void testServletCustomPrincipal() {
        String resp = readFromServer("/servlet");
        assertTrue(isContainerPrincipalTypeInResponse(resp,false));
    }

    @Test
    public void testServletCustomCallerPrincipal() {
        String resp = readFromServer("/servlet?useCallerPrincipal");
        assertTrue(isContainerPrincipalTypeInResponse(resp,true));
    }

    @Test
    public void testEjbCustomPrincipal() {
        String resp = readFromServer("/ejb-servlet");
        assertTrue(isContainerPrincipalTypeInResponse(resp,false));
    }

    @Test
    public void testEjbCustomCallerPrincipal() {
        String resp = readFromServer("/ejb-servlet?useCallerPrincipal");
        assertTrue(isContainerPrincipalTypeInResponse(resp,true));
    }

    public boolean isContainerPrincipalTypeInResponse(String response, boolean isCallerPrincipalUsed) {
        String[] principalArray = response.split(",");
        String containerPrincipal = principalArray[0];
        String applicationPrincipal = principalArray[1];
        String inputApplicationPrincipal = isCallerPrincipalUsed ? "org.glassfish.soteria.test.CustomCallerPrincipal" : "org.glassfish.soteria.test.CustomPrincipal";
        boolean isContainerPricipalCorrect = containerPrincipal.contains("com.sun.enterprise.security.web.integration.WebPrincipal") ||
                containerPrincipal.contains("weblogic.security.principal.WLSUserImpl") ||
                containerPrincipal.contains("com.ibm.ws.security.authentication.principals.WSPrincipal") ||
                containerPrincipal.contains("org.jboss.security.SimplePrincipal") ||
                containerPrincipal.contains("org.jboss.security.SimpleGroup") ||
                containerPrincipal.contains("org.apache.tomee.catalina.TomcatSecurityService$TomcatUser") ||
                containerPrincipal.contains("javax.security.enterprise.CallerPrincipal") ||
                containerPrincipal.contains(inputApplicationPrincipal);
        boolean isApplicationPrincipalCorrect = applicationPrincipal.contains(inputApplicationPrincipal);
        return isContainerPricipalCorrect && isApplicationPrincipalCorrect;
    }
}
