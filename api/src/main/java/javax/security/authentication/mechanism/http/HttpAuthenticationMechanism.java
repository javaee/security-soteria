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
package javax.security.authentication.mechanism.http;

import static javax.security.auth.message.AuthStatus.SEND_SUCCESS;

import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.module.ServerAuthModule;
import javax.security.authentication.mechanism.http.annotation.AutoApplySession;
import javax.security.authentication.mechanism.http.annotation.RememberMe;
import javax.security.identitystore.IdentityStore;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <code>HttpAuthenticationMechanism</code> is a mechanism for obtaining a caller's
 * credentials in some way, using the HTTP protocol where necessary.
 * 
 * <p>
 * This is used to help in securing Servlet endpoints, including
 * endpoints that may be build on top of Servlet like JAX-RS endpoints and JSF views.
 * It specifically <b>is not</b> used for endpoints such as remote EJB beans or (JMS) message driven beans.
 * 
 * <p>
 * A <code>HttpAuthenticationMechanism</code> is essentially a Servlet specific and CDI enabled version of
 * the {@link ServerAuthModule} that adheres to the Server Container Profile. See the JASPIC spec for further
 * details on this.
 * 
 * <p>
 * Implementations of this class can notify the Servlet container about a succesful authentication by using the
 * {@link HttpMessageContext#notifyContainerAboutLogin(javax.security.CallerPrincipal, java.util.List)} method.
 * 
 * <p>
 * Implementations are expected and encouraged to delegate the actual credential validation and/or retrieval of the
 * caller name with optional groups to an {@link IdentityStore}. This is however <b>not</b> required and implementations
 * can either do the validation checks for authentication completely autonomously, or delegate only certain aspects of
 * the process to the store (e.g. use the store only for retrieving the groups an authenticated user is in).
 * 
 * @author Arjan Tijms
 *
 */
public interface HttpAuthenticationMechanism {

    /**
     * Authenticate an HTTP request.
     * 
     * <p>
     * This method is called in response to an HTTP client request for a resource, and is always invoked 
     * <strong>before</strong> any {@link Filter} or {@link HttpServlet}. Additionally this method is called
     * in response to {@link HttpServletRequest#authenticate(HttpServletResponse)}
     * 
     * <p>
     * Note that by default this method is <strong>always</strong> called for every request, independent of whether
     * the request is to a protected or non-protected resource, or whether a caller was successfully authenticated
     * before within the same HTTP session or not.
     * 
     * <p>
     * A CDI/Interceptor spec interceptor can be used to prevent calls to this method if needed. 
     * See {@link AutoApplySession} and {@link RememberMe} for two examples.
     * 
     * @param request contains the request the client has made
     * @param response contains the response that will be send to the client
     * @param httpMessageContext context for interacting with the container
     * @return the completion status of the processing performed by this method
     * @throws AuthException when the processing failed
     */
    AuthStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthException;
   
    /**
     * Secure the response, optionally.
     * 
     * <p>
     * This method is called to allow for any post processing to be done on the request, and is always invoked 
     * <strong>after</strong> any {@link Filter} or {@link HttpServlet}. 
     * 
     * <p>
     * Note that this method is only called when a (Servlet) resource has indeed been invoked, i.e. if a previous call
     * to <code>validateRequest</code> that was invoked before any {@link Filter} or {@link HttpServlet} returned SUCCESS.
     *  
     * @param request contains the request the client has made
     * @param response contains the response that will be send to the client
     * @param httpMessageContext context for interacting with the container
     * @return the completion status of the processing performed by this method
     * @throws AuthException when the processing failed
     */
    default AuthStatus secureResponse(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthException {
        return SEND_SUCCESS;
    }
    
    /**
     * Remove mechanism specific principals and credentials from the subject and any other state the mechanism
     * might have used.
     * 
     * <p>
     * This method is called in response to {@link HttpServletRequest#logout()} and gives the authentication mechanism
     * the option to remove any state associated with an earlier established authenticated identity. For example, an
     * authentication mechanism that stores state within a cookie can send remove that cookie here.
     * 
     * @param request contains the request the client has made
     * @param response contains the response that will be send to the client
     * @param httpMessageContext context for interacting with the container
     */
    default void cleanSubject(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
        httpMessageContext.cleanClientSubject();
    }

}
