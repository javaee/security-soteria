/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
package javax.security.identitystore.credential;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * <code>BasicAuthenticationCredential</code> extends <code>UsernamePasswordCredential</code>
 * to represent credentials used by HTTP Basic Authentication.
 */
public class BasicAuthenticationCredential extends UsernamePasswordCredential {

    /**
     * Constructor
     *
     * @param authorizationHeader HTTP Basic Authentication header
     */
    public BasicAuthenticationCredential(String authorizationHeader) {
        super(parseUsername(authorizationHeader), parsePassword(authorizationHeader));
    }

    /**
     * Utility for decoding the HTTP Basic Authentication header.
     *
     * @param authorizationHeader  The encoded header
     * @return The decoded header
     */
    private static String decodeHeader(String authorizationHeader) {
        final String BASIC_AUTH_CHARSET = "US-ASCII";

        if (null == authorizationHeader)
            throw new NullPointerException("authorization header");
        if (authorizationHeader.isEmpty())
            throw new IllegalArgumentException("authorization header is empty");
        Base64.Decoder decoder = Base64.getMimeDecoder();
        byte[] decodedBytes = decoder.decode(authorizationHeader);
        try {
            return new String(decodedBytes, BASIC_AUTH_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unknown Charset: " + BASIC_AUTH_CHARSET, e);
        }
    }

    /**
     * Utility for parsing the HTTP Basic Authentication username.
     *
     * @param authorizationHeader The encoded header
     * @return The username
     */
    private static String parseUsername(String authorizationHeader) {
        String decodedAuthorizationHeader = decodeHeader(authorizationHeader);
        int delimiterIndex = decodedAuthorizationHeader.indexOf(':');
        if (delimiterIndex > -1) {
            return decodedAuthorizationHeader.substring(0, delimiterIndex);
        } else {
            return decodedAuthorizationHeader;
        }
    }

    /**
     * Utility for parsing the HTTP Basic Authentication password.
     *
     * @param authorizationHeader The encoded header
     * @return The password
     */
    private static Password parsePassword(String authorizationHeader) {
        String decodedAuthorizationHeader = decodeHeader(authorizationHeader);
        int delimiterIndex = decodedAuthorizationHeader.indexOf(':');
        if (delimiterIndex > -1) {
            return new Password(decodedAuthorizationHeader.substring(delimiterIndex + 1));
        } else {
            return new Password("");
        }
    }
}
