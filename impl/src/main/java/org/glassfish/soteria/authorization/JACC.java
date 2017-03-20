package org.glassfish.soteria.authorization;

import java.security.CodeSource;
import java.security.Permission;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebRoleRefPermission;

public class JACC {

    public static Subject getSubject() {
        try {
            return (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
        } catch (PolicyContextException e) {
            throw new IllegalStateException(e);
        }

    }
    
    public static boolean isCallerInRole(String role) {
        return hasPermission(getSubject(), new WebRoleRefPermission("", role));
    }
    
    public static boolean hasPermission(Subject subject, Permission permission) {
        return Policy.getPolicy().implies(
            new ProtectionDomain(
                new CodeSource(null, (java.security.cert.Certificate[]) null),
                null, null, 
                subject.getPrincipals().toArray(new Principal[subject.getPrincipals().size()])
            ),
            permission);
    }
    
}
