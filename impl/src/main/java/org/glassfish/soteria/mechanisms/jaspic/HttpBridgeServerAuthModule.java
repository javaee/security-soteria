/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.soteria.mechanisms.jaspic;

import static javax.security.enterprise.AuthenticationStatus.NOT_DONE;
import static javax.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import static org.glassfish.soteria.mechanisms.jaspic.Jaspic.fromAuthenticationStatus;
import static org.glassfish.soteria.mechanisms.jaspic.Jaspic.setLastAuthenticationStatus;

import java.util.Map;

import javax.enterprise.inject.spi.CDI;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.soteria.cdi.spi.CDIPerRequestInitializer;
import org.glassfish.soteria.mechanisms.HttpMessageContextImpl;

/**
 *
 * @author Arjan Tijms
 *
 */
public class HttpBridgeServerAuthModule implements ServerAuthModule {

        private CallbackHandler handler;
        private final Class<?>[] supportedMessageTypes = new Class[] { HttpServletRequest.class, HttpServletResponse.class };
        private final CDIPerRequestInitializer cdiPerRequestInitializer;
        
        public HttpBridgeServerAuthModule(CDIPerRequestInitializer cdiPerRequestInitializer) {
            this.cdiPerRequestInitializer = cdiPerRequestInitializer;
        }
        
        @Override
        public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, @SuppressWarnings("rawtypes") Map options) throws AuthException {
            this.handler = handler;
            // options not supported.
        }

        /**
         * A Servlet Container Profile compliant implementation should return HttpServletRequest and HttpServletResponse, so
         * the delegation class {@link ServerAuthContext} can choose the right SAM to delegate to.
         */
        @Override
        public Class<?>[] getSupportedMessageTypes() {
            return supportedMessageTypes;
        }

        @Override
        public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {
            
            HttpMessageContext msgContext = new HttpMessageContextImpl(handler, messageInfo, clientSubject);
            
            if (cdiPerRequestInitializer != null) {
                cdiPerRequestInitializer.init(msgContext.getRequest());
            }
            
            AuthenticationStatus status = NOT_DONE;
            setLastAuthenticationStatus(msgContext.getRequest(), status);
                
            try {
                status = CDI.current()
                            .select(HttpAuthenticationMechanism.class).get()
                            .validateRequest(
                                msgContext.getRequest(), 
                                msgContext.getResponse(), 
                                msgContext);
            } catch (AuthenticationException e) {
                // In case of an explicit AuthException, status will
                // be set to SEND_FAILURE, for any other (non checked) exception
                // the status will be the default NOT_DONE
                setLastAuthenticationStatus(msgContext.getRequest(), SEND_FAILURE);
                throw (AuthException) new AuthException("Authentication failure in HttpAuthenticationMechanism").initCause(e);
            }
            
            setLastAuthenticationStatus(msgContext.getRequest(), status);
            
            return fromAuthenticationStatus(status);
        }

        @Override
        public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
            HttpMessageContext msgContext = new HttpMessageContextImpl(handler, messageInfo, null);

            try {
                AuthenticationStatus status = CDI.current()
                                                 .select(HttpAuthenticationMechanism.class).get()
                                                 .secureResponse(
                                                     msgContext.getRequest(), 
                                                     msgContext.getResponse(), 
                                                     msgContext);
                AuthStatus authStatus = fromAuthenticationStatus(status);
                if (authStatus == AuthStatus.SUCCESS) {
                    return AuthStatus.SEND_SUCCESS;
                }
                return authStatus;
            } catch (AuthenticationException e) {
                throw (AuthException) new AuthException("Secure response failure in HttpAuthenticationMechanism").initCause(e);
            } finally {
                if (cdiPerRequestInitializer != null) {
                    cdiPerRequestInitializer.destroy(msgContext.getRequest());
                }
            }

        }

        /**
         * Called in response to a {@link HttpServletRequest#logout()} call.
         *
         */
        @Override
        public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
            HttpMessageContext msgContext = new HttpMessageContextImpl(handler, messageInfo, subject);
            
            CDI.current()
               .select(HttpAuthenticationMechanism.class).get()
               .cleanSubject(msgContext.getRequest(), msgContext.getResponse(), msgContext);
        }

}
