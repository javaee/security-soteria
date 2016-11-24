/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.soteria.servlet;

import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static org.glassfish.soteria.Utils.isEmpty;
import static org.glassfish.soteria.mechanisms.jaspic.Jaspic.deregisterServerAuthModule;
import static org.glassfish.soteria.mechanisms.jaspic.Jaspic.registerServerAuthModule;

import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.glassfish.soteria.cdi.CdiExtension;
import org.glassfish.soteria.cdi.spi.CDIPerRequestInitializer;
import org.glassfish.soteria.cdi.spi.impl.LibertyCDIPerRequestInitializer;
import org.glassfish.soteria.mechanisms.jaspic.HttpBridgeServerAuthModule;
import org.glassfish.soteria.mechanisms.jaspic.Jaspic;

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
public class SamRegistrationInstaller implements ServletContainerInitializer, ServletContextListener {
    
    private static final Logger logger =  Logger.getLogger(SamRegistrationInstaller.class.getName());

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {

        // Obtain a reference to the CdiExtension that was used to see if
        // there's an enabled bean
        
        CDI<Object> cdi = null;
        try {
            cdi = CDI.current();
            
            if (logger.isLoggable(INFO)) {
                logger.log(INFO, 
                    // TODO: Get version from build
                    "Initializing Soteria 1.0-m02-SNAPSHOT for context ''{0}''", 
                    ctx.getContextPath());
            }
            
        } catch (IllegalStateException e) {
            // On GlassFish 4.1.1/Payara 4.1.1.161 CDI is not initialized (org.jboss.weld.Container#initialize is not called), 
            // and calling CDI.current() will throw an exception. It's no use to continue then.
            // TODO: Do we need to find out *why* the default module does not have CDI initialized?
            logger.log(FINEST, "CDI not available for app context id: " + Jaspic.getAppContextID(ctx), e);
            
            return;
        }
        
        CdiExtension cdiExtension = cdi.select(CdiExtension.class).get();

        if (cdiExtension.isHttpAuthenticationMechanismFound()) {

            // A SAM must be registered at this point, since the programmatically added
            // Listener is for some reason restricted (not allow) from calling
            // getVirtualServerName. At this point we're still allowed to call this.
            
            // TODO: Ask the Servlet EG to address this? Is there any ground for this restriction???
            
            CDIPerRequestInitializer cdiPerRequestInitializer = null;
            
            if (!isEmpty(System.getProperty("wlp.server.name"))) {
                // Hardcode server check for now. TODO: design/implement proper service loader/SPI for this
                cdiPerRequestInitializer = new LibertyCDIPerRequestInitializer();
                logger.log(INFO, "Running on Liberty - installing CDI request scope activator");
            }
            
            registerServerAuthModule(new HttpBridgeServerAuthModule(cdiPerRequestInitializer), ctx);
          
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