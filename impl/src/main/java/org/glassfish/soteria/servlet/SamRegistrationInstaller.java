package org.glassfish.soteria.servlet;

import static org.glassfish.soteria.mechanisms.jaspic.Jaspic.deregisterServerAuthModule;
import static org.glassfish.soteria.mechanisms.jaspic.Jaspic.registerServerAuthModule;

import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;

import org.glassfish.soteria.cdi.CdiExtension;
import org.glassfish.soteria.mechanisms.jaspic.HttpBridgeServerAuthModule;

/**
 * If an HttpAuthenticationMechanism implementation has been found on the classpath, this 
 * initializer installs a bridge SAM that delegates the validateRequest, secureResponse and
 * cleanSubject methods from the SAM to the HttpAuthenticationMechanism.
 * 
 * <p>
 * The bridge SAM uses <code>CDI.current()</code> to obtain the HttpAuthenticationMechanism, therefore
 * fully enabling CDI in the implementation of that interface.
 * 
 * @author Arjan Tijms
 *
 */
@WebListener
public class SamRegistrationInstaller implements ServletContainerInitializer, ServletContextListener {

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {

        // Obtain a reference to the CdiExtension that was used to see if
        // there's an enabled bean

        CdiExtension cdiExtension = CDI.current().select(CdiExtension.class).get();

        if (cdiExtension.isHttpAuthenticationMechanismFound()) {

            // A SAM must be registered at this point, since the programmatically added
            // Listener is for some reason restricted (not allow) from calling
            // getVirtualServerName. At this point we're still allowed to call this.
            
            // TODO: Ask the Servlet EG to address this? Is there any ground for this restriction???
            registerServerAuthModule(new HttpBridgeServerAuthModule(), ctx);
          
            // Add a listener so we can process the context destroyed event, which is needed
            // to de-register the SAM correctly.
            ctx.addListener(this);
        }

    }
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // noop
        
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        deregisterServerAuthModule(sce.getServletContext());
    }
    
}
