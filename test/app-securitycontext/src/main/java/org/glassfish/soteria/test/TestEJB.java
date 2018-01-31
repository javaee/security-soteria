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

import static java.util.Collections.singleton;
import static org.glassfish.soteria.test.Utils.getELProcessor;

import java.security.Principal;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;


@Stateless
// Required by GlassFish and Payara
@DeclareRoles({ "foo", "bar", "kaz" })
// JBoss EAP 6.1+ (WildFly 7+) defaults unchecked methods to DenyAll
@PermitAll
public class TestEJB {

    @Inject
    private SecurityContext securityContext;

    @Resource
    private EJBContext ejbContext;

    public Principal getUserPrincipalFromEJBContext() {
        try {
            return ejbContext.getCallerPrincipal();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isCallerInRoleFromEJBContext(String role) {
        try {
            return ejbContext.isCallerInRole(role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Principal getUserPrincipalFromSecContext() {
        return securityContext.getCallerPrincipal();
    }

    public boolean isCallerInRoleFromSecContext(String role) {
        return securityContext.isCallerInRole(role);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getAllDeclaredCallerRoles() {
        // Note: uses reflection to avoid adding server specific classes
        if (securityContext.getClass().getName().equals("org.glassfish.soteria.SecurityContextImpl")) {
            return (Set<String>) getELProcessor("securityContext", securityContext).eval("securityContext.allDeclaredCallerRoles");
        }
        
        return singleton("* getAllDeclaredCallerRoles only supported on RI *");
    }

}
