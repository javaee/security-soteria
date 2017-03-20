package org.glassfish.soteria.authorization;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

public class JACC {

    public static Subject getSubject() {
        try {
            return (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
        } catch (PolicyContextException e) {
            throw new IllegalStateException(e);
        }

    }
    
}
