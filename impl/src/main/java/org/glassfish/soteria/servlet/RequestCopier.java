/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.soteria.servlet;

import static java.util.Arrays.copyOf;
import static java.util.Collections.emptyMap;
import static java.util.Collections.list;
import static org.glassfish.soteria.Utils.isEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * This class copies all "base data" from a given request. The goal is that this copied data can be used
 * later to restore a request, by wrapping a new request and delegating methods that fetch data
 * from that request to the copied data.
 * 
 * @author Arjan Tijms
 *
 */
public final class RequestCopier {
    
    private RequestCopier() {}

    public static RequestData copy(HttpServletRequest request) {
        
        RequestData requestData = new RequestData();
        
        requestData.setCookies(copyCookies(request.getCookies()));
        requestData.setHeaders(copyHeaders(request));
        requestData.setParameters(copyParameters(request.getParameterMap()));
        requestData.setLocales(list(request.getLocales()));
        
        requestData.setMethod(request.getMethod());
        requestData.setRequestURL(request.getRequestURL().toString());
        requestData.setQueryString(request.getQueryString());
    
        return requestData;
    }
    
    
    private static Cookie[] copyCookies(Cookie[] cookies) {
        
        if (isEmpty(cookies)) {
            return cookies;
        }
        
        ArrayList<Cookie> copiedCookies = new ArrayList<>();
        for (Cookie cookie : cookies) {
            copiedCookies.add((Cookie)cookie.clone());
        }
        
        return copiedCookies.toArray(new Cookie[copiedCookies.size()]);
    }
    
    private static Map<String, List<String>> copyHeaders(HttpServletRequest request) {
    
        Map<String, List<String>> copiedHeaders = new HashMap<>();
        for (String headerName : list(request.getHeaderNames())) {
            copiedHeaders.put(headerName, list(request.getHeaders(headerName)));
        }
        
        return copiedHeaders;
    }
    
    private static Map<String, String[]> copyParameters(Map<String, String[]> parameters) {
        
        if (isEmptyMap(parameters)) {
            return emptyMap();
        }
        
        Map<String, String[]> copiedParameters = new HashMap<>();
        for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
            copiedParameters.put(parameter.getKey(), copyOf(parameter.getValue(), parameter.getValue().length));
        }
        
        return copiedParameters;
    }
    
    private static boolean isEmptyMap(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
    
}
