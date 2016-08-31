package org.glassfish.soteria.cdi.spi;

import javax.servlet.http.HttpServletRequest;

public interface CDIPerRequestInitializer {

    void init(HttpServletRequest request);
    void destroy(HttpServletRequest request);
    
}
