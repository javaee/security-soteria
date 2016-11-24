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

import java.util.List;
import java.util.Map;

import javax.security.CallerPrincipal;
import javax.security.SecurityContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;
import javax.security.identitystore.CredentialValidationResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <code>HttpMessageContext</code> contains all of the per-request state information and encapsulates the client request, 
 * server response, container handler for authentication callbacks, and the subject representing the caller.
 * 
 * @author Arjan Tijms
 *
 */
public interface HttpMessageContext {

    /**
     * Checks if the current request is to a protected resource or not. A protected resource
     * is a resource (e.g. a Servlet, JSF page, JSP page etc) for which a constraint has been defined
     * in e.g. <code>web.xml</code>.
     * 
     * @return true if a protected resource was requested, false if a public resource was requested.
     */
    boolean isProtected();

    /**
     * Checks if the current call to an authentication mechanism is the result from the 
     * application calling {@link SecurityContext#authenticate(HttpServletResponse, AuthenticationParameters)}.
     * <p>
     * If SecurityContext#authenticate was not called, the authentication mechanism may have been invoked by the 
     * container at the start of a request.
     * 
     * @return true if SecurityContext#authenticate was called, false if not.
     */
    boolean isAuthenticationRequest();

    /**
     * Checks if during the current request code has asked the runtime to register an authentication session.
     * 
     * @return true if code has asked to register an authentication session, false otherwise.
     */
    boolean isRegisterSession();

    /**
     * Asks the runtime to register an authentication session. This will automatically remember the logged-in status
     * as long as the current HTTP session remains valid. Without this being asked, a SAM has to manually re-authenticate
     * with the runtime at the start of each request.
     * <p>
     * Note that the user name and roles being asked is an implementation detail; there is no portable way to have
     * an auth context read back the user name and roles that were processed by the {@link CallbackHandler}.
     * 
     * @param callerName the user name for which authentication should be be remembered
     * @param groups the groups for which authentication should be remembered.
     */
    void setRegisterSession(String callerName, List<String> groups);

    void cleanClientSubject();

    /**
     * Returns the parameters that were provided with the SecurityContect#authenticate(AuthParameters) call.
     *  
     * @return the parameters that were provided with the SecurityContect#authenticate(AuthParameters) call, or a default instance. Never null.
     */
    AuthenticationParameters getAuthParameters();

    /**
     * Returns the handler that the runtime provided to auth context.
     * 
     * @return the handler that the runtime provided to auth context.
     */
    CallbackHandler getHandler();

    /**
     * Returns the module options that were set on the auth module to which this context belongs.
     * 
     * @return the module options that were set on the auth module to which this context belongs.
     */
    Map<String, String> getModuleOptions();

    /**
     * Returns the named module option that was set on the auth module to which this context belongs.
     * 
     * @param key name of the module option
     * 
     * @return the named module option that was set on the auth module to which this context belongs, or null if no option with that name was set.
     */
    String getModuleOption(String key);

    /**
     * Returns the message info instance for the current request.
     * 
     * @return the message info instance for the current request.
     */
    MessageInfo getMessageInfo();

    /**
     * Returns the subject for which authentication is to take place.
     * 
     * @return the subject for which authentication is to take place.
     */
    Subject getClientSubject();

    /**
     * Returns the request object associated with the current request.
     * 
     * @return the request object associated with the current request.
     */
    HttpServletRequest getRequest();
    
    void setRequest(HttpServletRequest request);
    
    HttpMessageContext withRequest(HttpServletRequest request);

    /**
     * Returns the response object associated with the current request.
     * 
     * @return the response object associated with the current request.
     */
    HttpServletResponse getResponse();
    
    void setResponse(HttpServletResponse response);
    
    /**
     * Sets the response status to SC_FOUND 302 (Found)
     * <p>
     * As a convenience this method returns SEND_CONTINUE, so this method can be used in
     * one fluent return statement from an {@link HttpAuthenticationMechanism}
     * 
     * @return {@link AuthStatus#SEND_CONTINUE}
     */
    AuthStatus redirect(String location);
    
    AuthStatus forward(String path);
    

    /**
     * Sets the response status to 401 (not found).
     * <p>
     * As a convenience this method returns SEND_FAILURE, so this method can be used in
     * one fluent return statement from an {@link HttpAuthenticationMechanism}
     * 
     * @return {@link AuthStatus#SEND_FAILURE}
     */
    AuthStatus responseUnAuthorized();

    /**
     * Sets the response status to 404 (not found).
     * <p>
     * As a convenience this method returns SEND_FAILURE, so this method can be used in
     * one fluent return statement from an {@link HttpAuthenticationMechanism}
     * 
     * @return {@link AuthStatus#SEND_FAILURE}
     */
    AuthStatus responseNotFound();

    /**
     * Asks the container to register the given caller name and groups in order to make
     * them available to the application for use with {@link HttpServletRequest#isUserInRole(String)} etc.
     *
     * <p>
     * Note that after this call returned, the authenticated identity will not be immediately active. This
     * will only take place (should not errors occur) after the {@link ServerAuthContext} or {@link ServerAuthModule}
     * in which this call takes place return control back to the runtime.
     * 
     * <p>
     * As a convenience this method returns SUCCESS, so this method can be used in
     * one fluent return statement from an auth module.
     * 
     * @param username the user name that will become the caller principal
     * @param roles the roles associated with the caller principal
     * @return {@link AuthStatus#SUCCESS}
     *
     */
    AuthStatus notifyContainerAboutLogin(String username, List<String> roles);
    
    AuthStatus notifyContainerAboutLogin(CallerPrincipal callerPrincipal, List<String> roles);
    
    AuthStatus notifyContainerAboutLogin(CredentialValidationResult result) throws AuthException;

    /**
     * Instructs the container to "do nothing".
     * 
     * <p>
     * This is a somewhat peculiar requirement of JASPIC, which incidentally almost no containers actually require
     * or enforce. 
     * 
     * <p>
     * When intending to do nothing, most JASPIC auth modules simply return "SUCCESS", but according to
     * the JASPIC spec the handler MUST have been used when returning that status. Because of this JASPIC
     * implicitly defines a "protocol" that must be followed in this case; 
     * invoking the CallerPrincipalCallback handler with a null as the username.
     * 
     * <p>
     * As a convenience this method returns SUCCESS, so this method can be used in
     * one fluent return statement from an auth module.
     * 
     * @return {@link AuthStatus#SUCCESS}
     */
    AuthStatus doNothing();
    
    CallerPrincipal getCallerPrincipal();

    List<String> getGroups();

}