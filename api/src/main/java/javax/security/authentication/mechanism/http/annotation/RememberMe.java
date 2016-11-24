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
package javax.security.authentication.mechanism.http.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.el.ELProcessor;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import javax.resource.spi.AuthenticationMechanism;
import javax.security.authentication.mechanism.http.HttpMessageContext;
import javax.security.identitystore.RememberMeIdentityStore;
import javax.servlet.http.Cookie;

/**
 * The RememberMe annotation provides an application the ability to declarative designate 
 * that an {@link AuthenticationMechanism} "remembers" the authentication and auto
 * applies this with every request.
 * 
 * <p>
 * The location where this authentication is remembered has to be set by enabling a bean in
 * the application that implements the {@link RememberMeIdentityStore} interface.
 * 
 * <p>
 * For the remember me function the credentials provided by the caller are exchanged for a token
 * which is send to the user as the value of a cookie, in a similar way to how the HTTP session ID is send.
 * It should be realized that this token effectively becomes the credential to establish the caller's
 * identity within the application and care should be taken to handle and store the token securely. E.g.
 * by using this feature with a secure transport (SSL/https), storing a strong hash instead of the actual
 * token, and implementing an expiration policy. 
 * 
 * <p>
 * This support is provided via an implementation of an interceptor spec interceptor that conducts the
 * necessary logic.
 * 
 * <p>
 * Example:
 * 
 * <pre>
 * <code>
 *     {@literal @}RequestScoped
 *     {@literal @}RememberMe
 *     public class CustomAuthenticationMechanism implements HttpAuthenticationMechanism {
 *         // ...
 *     }
 * </code>
 * </pre>
 * 
 * @author Arjan Tijms
 *
 */
@Inherited
@InterceptorBinding
@Retention(RUNTIME)
@Target(TYPE)
public @interface RememberMe {
    
    /**
     * Max age in seconds for the remember me cookie.
     * Defaults to one day.
     * 
     * @see Cookie#setMaxAge(int)
     * 
     * @return Max age in seconds
     * 
     */
    @Nonbinding
    int cookieMaxAgeSeconds() default 86400; // 1 day
    
    /**
     * Name of the remember me cookie.
     * 
     * @see Cookie#getName()
     * 
     * @return The name of the cookie
     */
    @Nonbinding
    String cookieName() default "JREMEMBERMEID";
    
    /**
     * EL expression to determine if remember me should be used. This is evaluated
     * for every request requiring authentication. The expression needs to evaluate
     * to a boolean outcome. All named CDI beans are available to the expression
     * as well as default classes as specified by EL 3.0 for the {@link ELProcessor}
     * and the implicit objects "self" which refers to the interceptor target and
     * "httpMessageContext" which refers to the current {@link HttpMessageContext}.
     * 
     * @return EL expression to determine if remember me should be used
     * 
     */
    @Nonbinding
    String isRememberMeExpression() default "";
}