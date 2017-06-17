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
package org.glassfish.soteria.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.regex.Pattern.quote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.interceptor.InvocationContext;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

/**
 * An assortment of various utility methods.
 *
 * @author Arjan Tijms
 *
 */
public final class Utils {
    
    public final static Method validateRequestMethod = getMethod(
        HttpAuthenticationMechanism.class,
        "validateRequest",
        HttpServletRequest.class, HttpServletResponse.class, HttpMessageContext.class);
        
    public final static Method cleanSubjectMethod = getMethod(
        HttpAuthenticationMechanism.class, 
        "cleanSubject",
        HttpServletRequest.class, HttpServletResponse.class, HttpMessageContext.class);

	private static final String ERROR_UNSUPPORTED_ENCODING = "UTF-8 is apparently not supported on this platform.";

    private Utils() {}

	public static boolean notNull(Object... objects) {
		for (Object object : objects) {
			if (object == null) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns true if the given string is null or is empty.
	 *
	 * @param string The string to be checked on emptiness.
	 * @return True if the given string is null or is empty.
	 */
	public static boolean isEmpty(String string) {
		return string == null || string.isEmpty();
	}

	/**
	 * Returns <code>true</code> if the given array is null or is empty.
	 *
	 * @param array The array to be checked on emptiness.
	 * @return <code>true</code> if the given array is null or is empty.
	 */
	public static boolean isEmpty(Object[] array) {
		return array == null || array.length == 0;
	}

	/**
	 * Returns <code>true</code> if the given collection is null or is empty.
	 *
	 * @param collection The collection to be checked on emptiness.
	 * @return <code>true</code> if the given collection is null or is empty.
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * Returns <code>true</code> if the given object equals one of the given objects.
	 * @param <T> The generic object type.
	 * @param object The object to be checked if it equals one of the given objects.
	 * @param objects The argument list of objects to be tested for equality.
	 * @return <code>true</code> if the given object equals one of the given objects.
	 */
	@SafeVarargs
	public static <T> boolean isOneOf(T object, T... objects) {
		for (Object other : objects) {
			if (object == null ? other == null : object.equals(other)) {
				return true;
			}
		}

		return false;
	}
	
	@SuppressWarnings("unchecked")
    public static <T> T getParam(InvocationContext invocationContext, int param) {
	    return (T) invocationContext.getParameters()[param];
	}

	public static String getBaseURL(HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		return url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
	}

	public static void redirect(HttpServletResponse response, String location) {
		try {
			response.sendRedirect(location);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static void redirect(HttpServletRequest request, HttpServletResponse response, String location) {
		try {
			if (isFacesAjaxRequest(request)) {
				response.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
				response.setDateHeader("Expires", 0);
				response.setHeader("Pragma", "no-cache"); // Backwards compatibility for HTTP 1.0.
				response.setContentType("text/xml");
				response.setCharacterEncoding(UTF_8.name());
				response.getWriter().printf(FACES_AJAX_REDIRECT_XML, location);
			}
			else {
				response.sendRedirect(location);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private static final Set<String> FACES_AJAX_HEADERS = unmodifiableSet("partial/ajax", "partial/process");
	private static final String FACES_AJAX_REDIRECT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		+ "<partial-response><redirect url=\"%s\"></redirect></partial-response>";
	
	public static boolean isFacesAjaxRequest(HttpServletRequest request) {
		return FACES_AJAX_HEADERS.contains(request.getHeader("Faces-Request"));
	}
	
	@SuppressWarnings("unchecked")
	public static <E> Set<E> unmodifiableSet(Object... values) {
		Set<E> set = new HashSet<>();

		for (Object value : values) {
			if (value instanceof Object[]) {
				for (Object item : (Object[]) value) {
					set.add((E) item);
				}
			}
			else if (value instanceof Collection<?>) {
				for (Object item : (Collection<?>) value) {
					set.add((E) item);
				}
			}
			else {
				set.add((E) value);
			}
		}

		return Collections.unmodifiableSet(set);
	}

	public static String encodeURL(String string) {
		if (string == null) {
			return null;
		}

		try {
			return URLEncoder.encode(string, UTF_8.name());
		}
		catch (UnsupportedEncodingException e) {
			throw new UnsupportedOperationException(ERROR_UNSUPPORTED_ENCODING, e);
		}
	}

	public static String decodeURL(String string) {
		if (string == null) {
			return null;
		}

		try {
			return URLDecoder.decode(string, UTF_8.name());
		}
		catch (UnsupportedEncodingException e) {
			throw new UnsupportedOperationException(ERROR_UNSUPPORTED_ENCODING, e);
		}
	}

	public static String getSingleParameterFromState(String state, String paramName) {
		Map<String, List<String>> requestStateParameters = getParameterMapFromState(state);

		List<String> parameterValues = requestStateParameters.get(paramName);
		if (!isEmpty(parameterValues)) {
			return parameterValues.get(0);
		}

		return null;
	}

	public static Map<String, List<String>> getParameterMapFromState(String state) {
		return toParameterMap(unserializeURLSafe(state));
	}

	/**
	 * Converts the given request query string to request parameter values map.
	 * @param queryString The request query string.
	 * @return The request query string as request parameter values map.
	 */
	public static Map<String, List<String>> toParameterMap(String queryString) {
		String[] parameters = queryString.split(quote("&"));
		Map<String, List<String>> parameterMap = new LinkedHashMap<>(parameters.length);

		for (String parameter : parameters) {
			if (parameter.contains("=")) {
				String[] pair = parameter.split(quote("="));
				String key = decodeURL(pair[0]);
				String value = (pair.length > 1 && !isEmpty(pair[1])) ? decodeURL(pair[1]) : "";
				List<String> values = parameterMap.computeIfAbsent(key, k -> new ArrayList<>(1));

				values.add(value);
			}
		}

		return parameterMap;
	}

	/**
	 * Converts the given request parameter values map to request query string.
	 * @param parameterMap The request parameter values map.
	 * @return The request parameter values map as request query string.
	 */
	public static String toQueryString(Map<String, List<String>> parameterMap) {
		StringBuilder queryString = new StringBuilder();

		for (Entry<String, List<String>> entry : parameterMap.entrySet()) {
			String name = encodeURL(entry.getKey());

			for (String value : entry.getValue()) {
				if (queryString.length() > 0) {
					queryString.append("&");
				}

				queryString.append(name).append("=").append(encodeURL(value));
			}
		}

		return queryString.toString();
	}

	public static String getSingleParameterFromQueryString(String queryString, String paramName) {
		if (!isEmpty(queryString)) {
			Map<String, List<String>> requestParameters = toParameterMap(queryString);
	
			if (!isEmpty(requestParameters.get(paramName))) {
				return requestParameters.get(paramName).get(0);
			}
		}
	
		return null;
	}

	/**
	 * Serialize the given string to the short possible unique URL-safe representation. The current implementation will
	 * decode the given string with UTF-8 and then compress it with ZLIB using "best compression" algorithm and then
	 * Base64-encode the resulting bytes without the <code>=</code> padding, whereafter the Base64 characters
	 * <code>+</code> and <code>/</code> are been replaced by respectively <code>-</code> and <code>_</code> to make it
	 * URL-safe (so that no platform-sensitive URL-encoding needs to be done when used in URLs).
	 * @param string The string to be serialized.
	 * @return The serialized URL-safe string, or <code>null</code> when the given string is itself <code>null</code>.
	 */
	public static String serializeURLSafe(String string) {
		if (string == null) {
			return null;
		}

		try {
			InputStream raw = new ByteArrayInputStream(string.getBytes(UTF_8));
			ByteArrayOutputStream deflated = new ByteArrayOutputStream();
			stream(raw, new DeflaterOutputStream(deflated, new Deflater(Deflater.BEST_COMPRESSION)));
			String base64 = DatatypeConverter.printBase64Binary(deflated.toByteArray());
			return base64.replace('+', '-').replace('/', '_').replace("=", "");
		}
		catch (IOException e) {
			// This will occur when ZLIB and/or UTF-8 are not supported, but this is not to be expected these days.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Unserialize the given serialized URL-safe string. This does the inverse of {@link #serializeURLSafe(String)}.
	 * @param string The serialized URL-safe string to be unserialized.
	 * @return The unserialized string, or <code>null</code> when the given string is by itself <code>null</code>.
	 * @throws IllegalArgumentException When the given serialized URL-safe string is not in valid format as returned by
	 * {@link #serializeURLSafe(String)}.
	 */
	public static String unserializeURLSafe(String string) {
		if (string == null) {
			return null;
		}

		try {
			String base64 = string.replace('-', '+').replace('_', '/') + "===".substring(0, string.length() % 4);
			InputStream deflated = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(base64));
			return new String(toByteArray(new InflaterInputStream(deflated)), UTF_8);
		}
		catch (UnsupportedEncodingException e) {
			// This will occur when UTF-8 is not supported, but this is not to be expected these days.
			throw new RuntimeException(e);
		}
		catch (Exception e) {
			// This will occur when the string is not in valid Base64 or ZLIB format.
			throw new IllegalArgumentException(e);
		}
	}

	public static long stream(InputStream input, OutputStream output) throws IOException {
		try (ReadableByteChannel inputChannel = Channels.newChannel(input);
			WritableByteChannel outputChannel = Channels.newChannel(output))
		{
			ByteBuffer buffer = ByteBuffer.allocateDirect(10240);
			long size = 0;

			while (inputChannel.read(buffer) != -1) {
				buffer.flip();
				size += outputChannel.write(buffer);
				buffer.clear();
			}

			return size;
		}
	}

	/**
	 * Read the given input stream into a byte array. The given input stream will implicitly be closed after streaming,
	 * regardless of whether an exception is been thrown or not.
	 * @param input The input stream.
	 * @return The input stream as a byte array.
	 * @throws IOException When an I/O error occurs.
	 */
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		stream(input, output);
		return output.toByteArray();
	}
	
   public static boolean isImplementationOf(Method implementationMethod, Method interfaceMethod) {
        return
            interfaceMethod.getDeclaringClass().isAssignableFrom(implementationMethod.getDeclaringClass()) &&
            interfaceMethod.getName().equals(implementationMethod.getName()) &&
            Arrays.equals(interfaceMethod.getParameterTypes(), implementationMethod.getParameterTypes());
    }
    
   public static Method getMethod(Class<?> base, String name, Class<?>... parameterTypes) {
        try {
            // Method literals in Java would be nice
            return base.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }
	
}
