package org.glassfish.soteria.mechanisms.jaspic;

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
import javax.security.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.soteria.mechanisms.HttpMessageContextImpl;

/**
 *
 * @author Arjan Tijms
 *
 */
public class HttpBridgeServerAuthModule implements ServerAuthModule {

	private CallbackHandler handler;
	private Map<String, String> options;
	private final Class<?>[] supportedMessageTypes = new Class[] { HttpServletRequest.class, HttpServletResponse.class };
	
	@Override
	@SuppressWarnings("unchecked")
	public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, @SuppressWarnings("rawtypes") Map options) throws AuthException {
		this.handler = handler;
		this.options = options;
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
	    HttpMessageContext msgContext = new HttpMessageContextImpl(handler, options, messageInfo, clientSubject);
		
		return CDI.current()
		          .select(HttpAuthenticationMechanism.class).get()
		          .validateRequest(msgContext.getRequest(), msgContext.getResponse(), msgContext);
	}

	@Override
	public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
	    HttpMessageContext msgContext = new HttpMessageContextImpl(handler, options, messageInfo, null);
        
        return CDI.current()
                  .select(HttpAuthenticationMechanism.class).get()
                  .secureResponse(msgContext.getRequest(), msgContext.getResponse(), msgContext);
	}

	/**
	 * Called in response to a {@link HttpServletRequest#logout()} call.
	 *
	 */
	@Override
	public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
	    HttpMessageContext msgContext = new HttpMessageContextImpl(handler, options, messageInfo, subject);
	    
	    CDI.current()
           .select(HttpAuthenticationMechanism.class).get()
           .cleanSubject(msgContext.getRequest(), msgContext.getResponse(), msgContext);
	}
	


}