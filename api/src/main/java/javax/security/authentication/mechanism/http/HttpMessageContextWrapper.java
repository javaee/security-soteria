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
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.identitystore.CredentialValidationResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is an implementation of the HttpMessageContext interface that
 * can be subclassed by developers wishing to provide extra or different
 * functionality.
 * <p>
 * All methods default to calling the wrapped object.
 *
 */
public class HttpMessageContextWrapper implements HttpMessageContext {

    private final HttpMessageContext httpMessageContext;
 
    public HttpMessageContextWrapper(HttpMessageContext httpMessageContext) {
        this.httpMessageContext = httpMessageContext;
    }
    
    public HttpMessageContext getWrapped() {
        return httpMessageContext;
    }

    @Override
    public boolean isProtected() {
        return getWrapped().isProtected();
    }

    @Override
    public boolean isAuthenticationRequest() {
        return getWrapped().isAuthenticationRequest();
    }

    @Override
    public boolean isRegisterSession() {
        return getWrapped().isRegisterSession();
    }

    @Override
    public void setRegisterSession(String callerName, List<String> groups) {
        getWrapped().setRegisterSession(callerName, groups);
    }

    @Override
    public void cleanClientSubject() {
        getWrapped().cleanClientSubject();
    }

    @Override
    public AuthenticationParameters getAuthParameters() {
        return getWrapped().getAuthParameters();
    }

    @Override
    public CallbackHandler getHandler() {
        return getWrapped().getHandler();
    }

    @Override
    public Map<String, String> getModuleOptions() {
        return getWrapped().getModuleOptions();
    }

    @Override
    public String getModuleOption(String key) {
        return getWrapped().getModuleOption(key);
    }

    @Override
    public MessageInfo getMessageInfo() {
        return getWrapped().getMessageInfo();
    }

    @Override
    public Subject getClientSubject() {
        return getWrapped().getClientSubject();
    }

    @Override
    public HttpServletRequest getRequest() {
        return getWrapped().getRequest();
    }

    @Override
    public void setRequest(HttpServletRequest request) {
        getWrapped().setRequest(request);
    }

    @Override
    public HttpMessageContext withRequest(HttpServletRequest request) {
        return getWrapped().withRequest(request);
    }

    @Override
    public HttpServletResponse getResponse() {
        return getWrapped().getResponse();
    }

    @Override
    public void setResponse(HttpServletResponse response) {
        getWrapped().setResponse(response);
    }

    @Override
    public AuthStatus redirect(String location) {
        return getWrapped().redirect(location);
    }

    @Override
    public AuthStatus forward(String path) {
        return getWrapped().forward(path);
    }

    @Override
    public AuthStatus responseUnAuthorized() {
        return getWrapped().responseUnAuthorized();
    }

    @Override
    public AuthStatus responseNotFound() {
        return getWrapped().responseNotFound();
    }

    @Override
    public AuthStatus notifyContainerAboutLogin(String username, List<String> roles) {
        return getWrapped().notifyContainerAboutLogin(username, roles);
    }

    @Override
    public AuthStatus notifyContainerAboutLogin(CallerPrincipal callerPrincipal, List<String> roles) {
        return getWrapped().notifyContainerAboutLogin(callerPrincipal, roles);
    }

    @Override
    public AuthStatus notifyContainerAboutLogin(CredentialValidationResult result) throws AuthException {
        return getWrapped().notifyContainerAboutLogin(result);
    }

    @Override
    public AuthStatus doNothing() {
        return getWrapped().doNothing();
    }

    @Override
    public CallerPrincipal getCallerPrincipal() {
        return getWrapped().getCallerPrincipal();
    }

    @Override
    public List<String> getGroups() {
        return getWrapped().getGroups();
    }
    
}
