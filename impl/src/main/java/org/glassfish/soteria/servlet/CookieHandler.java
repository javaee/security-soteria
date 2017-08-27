/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.soteria.servlet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.soteria.Utils;


public class CookieHandler {

	public static void saveCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, Integer maxAge, boolean secureOnly, boolean HttpOnly) {
		Cookie cookie = new Cookie(name, value);
		if (maxAge != null) {
			cookie.setMaxAge(maxAge);
		}
		cookie.setSecure(secureOnly);
		cookie.setHttpOnly(HttpOnly);
		cookie.setPath(Utils.isEmpty(request.getContextPath())? "/" : request.getContextPath());

		response.addCookie(cookie);
	}
	
	public static Cookie getCookie(HttpServletRequest request, String name) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (name.equals(cookie.getName()) && !isEmpty(cookie)) {
					return cookie;
				}
			}
		}

		return null;
	}

	public static void removeCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		Cookie cookie = new Cookie(name, null);
		cookie.setMaxAge(0);
		cookie.setPath(Utils.isEmpty(request.getContextPath())? "/" : request.getContextPath());

		response.addCookie(cookie);
	}
	
	private static boolean isEmpty(Cookie cookie) {
		return cookie.getValue() == null || cookie.getValue().trim().isEmpty();
	}
	
}
