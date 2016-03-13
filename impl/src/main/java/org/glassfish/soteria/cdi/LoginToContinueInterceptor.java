package org.glassfish.soteria.cdi;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;
import static javax.security.auth.message.AuthStatus.FAILURE;
import static javax.security.auth.message.AuthStatus.SUCCESS;
import static javax.security.identitystore.CredentialValidationResult.Status.VALID;
import static org.glassfish.soteria.Utils.getBaseURL;
import static org.glassfish.soteria.Utils.getParam;
import static org.glassfish.soteria.Utils.isImplementationOf;
import static org.glassfish.soteria.Utils.notNull;
import static org.glassfish.soteria.Utils.validateRequestMethod;
import static org.glassfish.soteria.cdi.CdiUtils.getAnnotation;
import static org.glassfish.soteria.servlet.RequestCopier.copy;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.authentication.mechanism.http.HttpMessageContext;
import javax.security.authentication.mechanism.http.annotation.LoginToContinue;
import javax.security.identitystore.CredentialValidationResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.glassfish.soteria.mechanisms.LoginToContinueHolder;
import org.glassfish.soteria.servlet.HttpServletRequestDelegator;
import org.glassfish.soteria.servlet.RequestData;


@Interceptor
@LoginToContinue
@Priority(PLATFORM_BEFORE + 220)
public class LoginToContinueInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Inject
    private BeanManager beanManager;
    
    @Inject
    @Intercepted
    private Bean<?> interceptedBean;

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        
        // If intercepting HttpAuthenticationMechanism#validateRequest
        if (isImplementationOf(invocationContext.getMethod(), validateRequestMethod)) {
            return validateRequest(
                invocationContext, 
                getParam(invocationContext, 0),  
                getParam(invocationContext, 1),
                getParam(invocationContext, 2));
        }
        
        return invocationContext.proceed();
    }
    
    private AuthStatus validateRequest(InvocationContext invocationContext, HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws Exception {
        
        // 0. Caller aborted earlier flow and does a new request to protected resource
        if (isOnProtectedURLWithStaleData(httpMessageContext)) {
            removeSavedRequest(request);
        }
        
        // 1. Protected resource requested and no request saved before
        if (isOnInitialProtectedURL(httpMessageContext)) {
            // TODO: request.authenticate() is captured by this as well
            // Use an "initial call tracker interceptor"?
            
            // Save request details and redirect/forward to /login page
            saveRequest(request);
            
            // TODO: option to redirect instead of forward?
            // TODO: Use modified request/response for forward to set method to GET and filter out "if-" headers?
            return httpMessageContext.forward(
                getLoginToContinueAnnotation(invocationContext).loginPage());
        }
        
        
        // 2. A postback after we have redirected the caller in step 1.
        //    NOTE: this does not have to be the resource we redirected the caller to.
        //          E.g. we can redirect to /login, and /login can postback to J_SECURITY_CHECK or /login2,
        //          or whatever. For each such postback we give the authentication mechanism the opportunity
        //          to authenticate though.
        if (isOnLoginPostback(request)) {
            // Try to authenticate with the next interceptor or actual authentication mechanism
            AuthStatus authstatus = null;
            
            try {
                authstatus = (AuthStatus) invocationContext.proceed();
            } catch (AuthException e) {
                authstatus = FAILURE;
            }
          
            if (authstatus == SUCCESS) {
                
                if (httpMessageContext.getCallerPrincipal() == null) {
                    return SUCCESS;
                }
                
                // Authentication was successful and an actual caller principal was set 
                RequestData savedRequest = getSavedRequest(request);
                
                // Check if we're already on the right target URL
                if  (!savedRequest.matchesRequest(request)) {
                
                    // Store the authenticated data before redirecting to the right
                    // URL. This is needed since the underlying JASPIC runtime does not
                    // remember the authenticated identity if we redirect.
                    saveAuthentication(request, new CredentialValidationResult(
                        VALID,
                        httpMessageContext.getCallerPrincipal(),
                        httpMessageContext.getGroups()));
                    
                    return httpMessageContext.redirect(savedRequest.getFullRequestURL());
                } // else return success
                
            } else {
                return httpMessageContext.redirect( // TODO: or forward?
                        // TODO: option for error parameter instead, e.g. /login?error=true
                       
                        getBaseURL(request) +
                        getLoginToContinueAnnotation(invocationContext).errorPage());
            }
             
        }
        
        
        // 3. Authenticated data saved and back on original URL from step 1.
        if (isOnOriginalURLAfterAuthenticate(request)) {
            
            // Remove all the data we saved
            RequestData requestData = removeSavedRequest(request);
            CredentialValidationResult result = removeSavedAuthentication(request);
            
            // Wrap the request to provide all the original request data again, such as the original
            // headers and the HTTP method, authenticate and then invoke the originally requested resource
            return httpMessageContext
                .withRequest(new HttpServletRequestDelegator(request, requestData))
                .notifyContainerAboutLogin(
                    result.getCallerPrincipal(), 
                    result.getCallerGroups());
            
        }
       
        return httpMessageContext.doNothing();
    }
    
    private boolean isOnProtectedURLWithStaleData(HttpMessageContext httpMessageContext) {
        return
            httpMessageContext.isProtected() && 
            getSavedRequest(httpMessageContext.getRequest()) != null &&
            getSavedAuthentication(httpMessageContext.getRequest()) == null &&
            // Some servers consider the Servlet special URL "/j_security_check" as
            // a protected URL
            
            !httpMessageContext.getRequest().getRequestURI().endsWith("j_security_check");
    }
    
    private boolean isOnInitialProtectedURL(HttpMessageContext httpMessageContext) {
        return 
            httpMessageContext.isProtected() && 
            getSavedRequest(httpMessageContext.getRequest()) == null;
    }
    
    private boolean isOnLoginPostback(HttpServletRequest request) {
        return 
            getSavedRequest(request) != null &&
            getSavedAuthentication(request) == null;
    }
    
    private boolean isOnOriginalURLAfterAuthenticate(HttpServletRequest request) {
        
        RequestData savedRequest = getSavedRequest(request);
        CredentialValidationResult credentialValidationResult = getSavedAuthentication(request);
        
        return
            notNull(savedRequest, credentialValidationResult) && 
            savedRequest.matchesRequest(request);
        
    }
    
    private LoginToContinue getLoginToContinueAnnotation(InvocationContext invocationContext) {
        
        if (invocationContext.getTarget() instanceof LoginToContinueHolder) {
            return ((LoginToContinueHolder) invocationContext.getTarget()).getLoginToContinue();
        }
        
        Optional<LoginToContinue> optionalLoginToContinue = getAnnotation(beanManager, interceptedBean.getBeanClass(), LoginToContinue.class);
        if (optionalLoginToContinue.isPresent()) {
            return optionalLoginToContinue.get();
        }
        
        throw new IllegalStateException("@LoginToContinue not present on " + interceptedBean.getBeanClass());
    }
    
    private static final String ORIGINAL_REQUEST_DATA_SESSION_NAME = "org.glassfish.soteria.original.request";
    private static final String AUTHENTICATION_DATA_SESSION_NAME = "org.glassfish.soteria.authentication";

    private void saveRequest(HttpServletRequest request) {
        request.getSession().setAttribute(ORIGINAL_REQUEST_DATA_SESSION_NAME, copy(request));
    }

    private RequestData getSavedRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return (RequestData) session.getAttribute(ORIGINAL_REQUEST_DATA_SESSION_NAME);
    }

    private RequestData removeSavedRequest(HttpServletRequest request) {
        RequestData requestData = getSavedRequest(request);
        
        request.getSession().removeAttribute(ORIGINAL_REQUEST_DATA_SESSION_NAME);
        
        return requestData;
    }
    
    private void saveAuthentication(HttpServletRequest request, CredentialValidationResult credentialValidationResult) {
        request.getSession().setAttribute(AUTHENTICATION_DATA_SESSION_NAME, credentialValidationResult);
    }

    private CredentialValidationResult getSavedAuthentication(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return (CredentialValidationResult) session.getAttribute(AUTHENTICATION_DATA_SESSION_NAME);
    }

    private CredentialValidationResult removeSavedAuthentication(HttpServletRequest request) {
        CredentialValidationResult result = getSavedAuthentication(request);
        
        request.getSession().removeAttribute(AUTHENTICATION_DATA_SESSION_NAME);
        
        return result;
    }
 
}