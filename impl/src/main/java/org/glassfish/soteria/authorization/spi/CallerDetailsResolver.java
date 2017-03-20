package org.glassfish.soteria.authorization.spi;

import java.security.Principal;

public interface CallerDetailsResolver {
    
    Principal getCallerPrincipal();
    boolean isCallerInRole(String role);

}
