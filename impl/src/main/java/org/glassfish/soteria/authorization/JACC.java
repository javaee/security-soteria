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
package org.glassfish.soteria.authorization;

import static java.security.Policy.getPolicy;
import static java.util.Collections.list;
import static org.glassfish.soteria.authorization.EJB.getCurrentEJBName;
import static org.glassfish.soteria.authorization.EJB.getEJBContext;

import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJBContext;
import javax.security.auth.Subject;
import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;

public class JACC {

    public static Subject getSubject() {
        return getFromContext("javax.security.auth.Subject.container");
    }

    public static boolean isCallerInRole(String role) {
        
        Subject subject = getSubject();
        
        if (hasPermission(subject, new WebRoleRefPermission("", role))) {
            return true;
        }
        
        EJBContext ejbContext = getEJBContext();
        
        if (ejbContext != null) {
            
            // We're called from an EJB
            
            // To ask for the permission, get the EJB name first.
            // Unlike the Servlet container there's no automatic mapping
            // to a global ("") name.
            String ejbName = getCurrentEJBName(ejbContext);
            if (ejbName != null) {
                return hasPermission(subject, new EJBRoleRefPermission(ejbName, role));
            }
            
            // EJB name not supported for current container, fallback to going fully through
            // ejbContext
            return ejbContext.isCallerInRole(role);
        }
        
        return false;
    }

    public static boolean hasAccessToWebResource(String resource, String... methods) {
        return hasPermission(getSubject(), new WebResourcePermission(resource, methods));
    }

    public static Set<String> getAllDeclaredCallerRoles() {
        // Get the permissions associated with the Subject we obtained
        PermissionCollection permissionCollection = getPermissionCollection(getSubject());

        // Resolve any potentially unresolved role permissions
        permissionCollection.implies(new WebRoleRefPermission("", "nothing"));
        permissionCollection.implies(new EJBRoleRefPermission("", "nothing"));
        
        // Filter just the roles from all the permissions, which may include things like 
        // java.net.SocketPermission, java.io.FilePermission, and obtain the actual role names.
        return filterRoles(permissionCollection);

    }

    public static boolean hasPermission(Subject subject, Permission permission) {
        return getPolicy().implies(fromSubject(subject), permission);
    }

    public static PermissionCollection getPermissionCollection(Subject subject) {
        return getPolicy().getPermissions(fromSubject(subject));
    }

    public static Set<String> filterRoles(PermissionCollection permissionCollection) {
        Set<String> roles = new HashSet<>();
        for (Permission permission : list(permissionCollection.elements())) {
            if (isRolePermission(permission)) {
                String role = permission.getActions();

                // Note that the WebRoleRefPermission is given for every Servlet in the application, even when
                // no role refs are used anywhere. This will also include Servlets like the default servlet and the
                // implicit JSP servlet. So if there are 2 application roles, and 3 application servlets, then 
                // at least 6 WebRoleRefPermission elements will be present in the collection.
                if (!roles.contains(role) && isCallerInRole(role)) {
                    roles.add(role);
                }
            }
        }

        return roles;
    }

    public static ProtectionDomain fromSubject(Subject subject) {
        return new ProtectionDomain(
                new CodeSource(null, (Certificate[]) null),
                null, null,
                subject.getPrincipals().toArray(new Principal[subject.getPrincipals().size()])
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFromContext(String contextName) {
        try {
            return (T) PolicyContext.getContext(contextName);
        } catch (PolicyContextException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public static boolean isRolePermission(Permission permission) {
        return permission instanceof WebRoleRefPermission || permission instanceof EJBRoleRefPermission;
    }
    
  

}
