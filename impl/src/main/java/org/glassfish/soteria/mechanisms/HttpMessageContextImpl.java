package org.glassfish.soteria.mechanisms;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static javax.security.auth.message.AuthStatus.SEND_CONTINUE;
import static javax.security.auth.message.AuthStatus.SEND_FAILURE;
import static javax.security.auth.message.AuthStatus.SUCCESS;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.security.CallerPrincipal;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.authentication.mechanism.http.AuthenticationParameters;
import javax.security.authentication.mechanism.http.HttpMessageContext;
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
 *
 */
public class HttpMessageContextImpl implements HttpMessageContext {

    private CallbackHandler handler;
    private Map<String, String> moduleOptions;
	private MessageInfo messageInfo; 
    private Subject clientSubject;
    private AuthenticationParameters authParameters;

    private CallerPrincipal callerPrincipal;
    private List<String> groups;
    
    public HttpMessageContextImpl(CallbackHandler handler, Map<String, String> moduleOptions, MessageInfo messageInfo, Subject clientSubject) {
        this.handler = handler;
        if (moduleOptions != null) {
        	this.moduleOptions = unmodifiableMap(moduleOptions);
        } else {
        	this.moduleOptions = emptyMap();
        }
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
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#setRegisterSession(java.lang.String, java.util.List)
     */
    @Override
    public void setRegisterSession(String username, List<String> roles) {
        Jaspic.setRegisterSession(messageInfo, username, roles);
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
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#getModuleOptions()
     */
    @Override
    public Map<String, String> getModuleOptions() {
		return moduleOptions;
	}
    
    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#getModuleOption(java.lang.String)
     */
    @Override
    public String getModuleOption(String key) {
    	return moduleOptions.get(key);
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
    public AuthStatus redirect(String location) {
        Utils.redirect(getResponse(), location);
        
        return SEND_CONTINUE;
    }
    
    @Override
    public AuthStatus forward(String path) {
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
    public AuthStatus responseUnAuthorized() {
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
    public AuthStatus responseNotFound() {
    	try {
			getResponse().sendError(SC_NOT_FOUND);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
    	
    	return SEND_FAILURE;
    }
    
    /* (non-Javadoc)
     * @see javax.security.authenticationmechanism.http.HttpMessageContext#notifyContainerAboutLogin(java.lang.String, java.util.List)
     */
    @Override
    public AuthStatus notifyContainerAboutLogin(String callerName, List<String> groups) {
        CallerPrincipal callerPrincipal = null;
        if (callerName != null) {
            callerPrincipal = new CallerPrincipal(callerName); // TODO: or store username separately?
        } 
        
        return notifyContainerAboutLogin(callerPrincipal, groups);
    }
    
    @Override
    public AuthStatus notifyContainerAboutLogin(CallerPrincipal callerPrincipal, List<String> groups) {
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
    public AuthStatus doNothing() {
        this.callerPrincipal = null;
        this.groups = null;
        
    	Jaspic.notifyContainerAboutLogin(clientSubject, handler, (String) null, null);
    	
    	return SUCCESS;
    }
    
    @Override
    public CallerPrincipal getCallerPrincipal() {
        return callerPrincipal;
    }

    @Override
    public List<String> getGroups() {
        return groups;
    }

}