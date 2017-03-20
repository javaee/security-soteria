package org.glassfish.soteria.authorization.spi.impl;

import static java.util.Collections.emptyList;
import static javax.security.jacc.PolicyContext.getContextID;

import java.security.Principal;

import javax.security.auth.Subject;

import org.glassfish.soteria.authorization.JACC;
import org.glassfish.soteria.authorization.spi.CallerDetailsResolver;

public class ReflectionAndJaccCallerDetailsResolver implements CallerDetailsResolver {

    @Override
    public Principal getCallerPrincipal() {
        Subject subject = JACC.getSubject();
        
        if (subject == null) {
            return null;
        }
        
        SubjectParser roleMapper = new SubjectParser(getContextID(), emptyList());
        
        return roleMapper.getCallerPrincipalFromPrincipals(subject.getPrincipals());
    }
    
    @Override
    public boolean isCallerInRole(String role) {
        // TODO Auto-generated method stub
        return false;
    }
    
}
