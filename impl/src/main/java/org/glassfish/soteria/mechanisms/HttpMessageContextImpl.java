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
package org.glassfish.soteria.mechanisms;

import static javax.security.enterprise.AuthenticationStatus.NOT_DONE;
import static javax.security.enterprise.AuthenticationStatus.SEND_CONTINUE;
import static javax.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import static javax.security.enterprise.AuthenticationStatus.SUCCESS;
import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.MessageInfo;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.soteria.Utils;
import org.glassfish.soteria.mechanisms.jaspic.Jaspic;

/**
 * A convenience context that provides access to JASPIC Servlet Profile specific types
 * and functionality.
 *
 * @author Arjan Tijms
 */
public class HttpMessageContextImpl implements HttpMessageContext {

    private CallbackHandler handler;
    private MessageInfo messageInfo;
    private Subject clientSubject;
    private AuthenticationParameters authParameters;

    private CallerPrincipal callerPrincipal;
    private Set<String> groups;

    public HttpMessageContextImpl(CallbackHandler handler, MessageInfo messageInfo, Subject clientSubject) {
        this.handler = handler;
        this.messageInfo = messageInfo;
        this.clientSubject = clientSubject;
        if (messageInfo != null) {
            this.authParameters = Jaspic.getAuthParameters(getRequest());
        }
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#isProtected()
     */
    @Override
    public boolean isProtected() {
        return Jaspic.isProtectedResource(messageInfo);
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#isAuthenticationRequest()
     */
    @Override
    public boolean isAuthenticationRequest() {
        return Jaspic.isAuthenticationRequest(getRequest());
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#isRegisterSession()
     */
    @Override
    public boolean isRegisterSession() {
        return Jaspic.isRegisterSession(messageInfo);
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#setRegisterSession(java.lang.String, java.util.Set)
     */
    @Override
    public void setRegisterSession(String username, Set<String> groups) {
        Jaspic.setRegisterSession(messageInfo, username, groups);
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#cleanClientSubject()
     */
    @Override
    public void cleanClientSubject() {
        Jaspic.cleanSubject(clientSubject);
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#getAuthParameters()
     */
    @Override
    public AuthenticationParameters getAuthParameters() {
        return authParameters;
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#getHandler()
     */
    @Override
    public CallbackHandler getHandler() {
        return handler;
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#getMessageInfo()
     */
    @Override
    public MessageInfo getMessageInfo() {
        return messageInfo;
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#getClientSubject()
     */
    @Override
    public Subject getClientSubject() {
        return clientSubject;
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#getRequest()
     */
    @Override
    public HttpServletRequest getRequest() {
        return (HttpServletRequest) messageInfo.getRequestMessage();
    }

    @Override
    public void setRequest(HttpServletRequest request) {
        messageInfo.setRequestMessage(request);
    }

    @Override
    public HttpMessageContext withRequest(HttpServletRequest request) {
        setRequest(request);
        return this;
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#getResponse()
     */
    @Override
    public HttpServletResponse getResponse() {
        return (HttpServletResponse) messageInfo.getResponseMessage();
    }

    @Override
    public void setResponse(HttpServletResponse response) {
        messageInfo.setResponseMessage(response);
    }

    @Override
    public AuthenticationStatus redirect(String location) {
        Utils.redirect(getResponse(), location);

        return SEND_CONTINUE;
    }

    @Override
    public AuthenticationStatus forward(String path) {
        try {
            getRequest().getRequestDispatcher(path)
                    .forward(getRequest(), getResponse());
        } catch (IOException | ServletException e) {
            throw new IllegalStateException(e);
        }

        // After forward MUST NOT invoke the resource, so CAN NOT return SUCCESS here.
        return SEND_CONTINUE;
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#responseUnAuthorized()
     */
    @Override
    public AuthenticationStatus responseUnauthorized() {
        try {
            getResponse().sendError(SC_UNAUTHORIZED);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return SEND_FAILURE;
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#responseNotFound()
     */
    @Override
    public AuthenticationStatus responseNotFound() {
        try {
            getResponse().sendError(SC_NOT_FOUND);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return SEND_FAILURE;
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#notifyContainerAboutLogin(java.lang.String, java.util.Set)
     */
    @Override
    public AuthenticationStatus notifyContainerAboutLogin(String callerName, Set<String> groups) {
        CallerPrincipal callerPrincipal = null;
        if (callerName != null) {
            callerPrincipal = new CallerPrincipal(callerName); // TODO: or store username separately?
        }

        return notifyContainerAboutLogin(callerPrincipal, groups);
    }

    @Override
    public AuthenticationStatus notifyContainerAboutLogin(CredentialValidationResult result) {
        if (result.getStatus() == VALID) {
            return notifyContainerAboutLogin(
                    result.getCallerPrincipal(),
                    result.getCallerGroups());

        } 
            
        return SEND_FAILURE;
    }

    @Override
    public AuthenticationStatus notifyContainerAboutLogin(CallerPrincipal callerPrincipal, Set<String> groups) {
        this.callerPrincipal = callerPrincipal;
        if (callerPrincipal != null) {
            this.groups = groups;
        } else {
            this.groups = null;
        }

        Jaspic.notifyContainerAboutLogin(clientSubject, handler, callerPrincipal, groups);

        // Explicitly set a flag that we did authentication, so code can check that this happened
        // TODO: or throw CDI event here?
        Jaspic.setDidAuthentication((HttpServletRequest) messageInfo.getRequestMessage());

        return SUCCESS;
    }

    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#doNothing()
     */
    @Override
    public AuthenticationStatus doNothing() {
        this.callerPrincipal = null;
        this.groups = null;

        Jaspic.notifyContainerAboutLogin(clientSubject, handler, (String) null, null);

        return NOT_DONE;
    }

    @Override
    public CallerPrincipal getCallerPrincipal() {
        return callerPrincipal;
    }

    @Override
    public Set<String> getGroups() {
        return groups;
    }

}