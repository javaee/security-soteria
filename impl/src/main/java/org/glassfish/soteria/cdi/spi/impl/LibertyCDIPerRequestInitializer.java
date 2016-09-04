package org.glassfish.soteria.cdi.spi.impl;

import javax.el.ELProcessor;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

import org.glassfish.soteria.cdi.spi.CDIPerRequestInitializer;

/**
 * Hacky (but working) CDI initializer for Liberty. Should probably be moved to an SPI jar
 * later using the necessary Weld types directly and/or implemented by Liberty, should
 * Liberty decide to use and/or support Soteria.  
 * 
 * @author arjan
 *
 */
public class LibertyCDIPerRequestInitializer implements CDIPerRequestInitializer  {

    @Override
    public void init(HttpServletRequest request) {
        Object weldInitialListener = request.getServletContext().getAttribute("org.jboss.weld.servlet.WeldInitialListener");
        ServletRequestEvent event = new ServletRequestEvent(request.getServletContext(), request);
                 
        ELProcessor elProcessor = new ELProcessor();
        elProcessor.defineBean("weldInitialListener", weldInitialListener);
        elProcessor.defineBean("event", event);
        elProcessor.eval("weldInitialListener.requestInitialized(event)");
    }
    
    @Override
    public void destroy(HttpServletRequest request) {
        Object weldInitialListener = request.getServletContext().getAttribute("org.jboss.weld.servlet.WeldInitialListener");
        ServletRequestEvent event = new ServletRequestEvent(request.getServletContext(), request);
                 
        ELProcessor elProcessor = new ELProcessor();
        elProcessor.defineBean("weldInitialListener", weldInitialListener);
        elProcessor.defineBean("event", event);
        elProcessor.eval("weldInitialListener.requestDestroyed(event)");
    }
    
}
