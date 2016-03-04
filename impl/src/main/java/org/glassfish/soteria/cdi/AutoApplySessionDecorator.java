package org.glassfish.soteria.cdi;

import static java.lang.Boolean.TRUE;
import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static javax.security.auth.message.AuthStatus.SUCCESS;

import java.io.IOException;
import java.security.Principal;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Decorator
@Priority(PLATFORM_BEFORE + 200)
public class AutoApplySessionDecorator extends HttpAuthenticationBaseDecorator {

    private static final long serialVersionUID = 1L;

    @Inject
    @Delegate
    private HttpAuthenticationMechanism delegateMechanism;

    public AutoApplySessionDecorator() {
    }

    public AutoApplySessionDecorator(HttpAuthenticationMechanism delegateMechanism) {
        this.delegateMechanism = delegateMechanism;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AuthStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthException {

        Principal userPrincipal = httpMessageContext.getRequest().getUserPrincipal();

        if (userPrincipal != null) {

            try {
                httpMessageContext.getHandler().handle(new Callback[] {
                        new CallerPrincipalCallback(httpMessageContext.getClientSubject(), userPrincipal) });
            } catch (IOException | UnsupportedCallbackException e) {
                e.printStackTrace();
            }

            return SUCCESS;
        }

        AuthStatus outcome = delegateMechanism.validateRequest(request, response, httpMessageContext);

        if (SUCCESS.equals(outcome)) {
            httpMessageContext.getMessageInfo().getMap().put("javax.servlet.http.registerSession", TRUE.toString());
        }

        return outcome;
    }
}