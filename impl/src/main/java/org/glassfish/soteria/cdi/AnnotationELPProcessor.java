/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.soteria.cdi;

import static java.util.Arrays.stream;
import static org.glassfish.soteria.Utils.isEmpty;
import static org.glassfish.soteria.cdi.CdiUtils.getELProcessor;

import java.lang.reflect.Array;

import javax.el.ELProcessor;

public class AnnotationELPProcessor {
    
    public static String evalImmediate(String expression) {
        return evalImmediate((ELProcessor)null, expression);
    }
    
    public static String evalImmediate(ELProcessor getELProcessor, String expression) {
        if (!isELExpression(expression) || isDeferredExpression(expression)) {
            return expression;
        }
        
        return (String) getELProcessor(getELProcessor).eval(toRawExpression(expression));
    }
    
    public static boolean evalImmediate(String expression, boolean defaultValue) {
        return evalImmediate(null, expression, defaultValue);
    }
    
    public static boolean evalImmediate(ELProcessor getELProcessor, String expression, boolean defaultValue) {
        if (!isELExpression(expression) || isDeferredExpression(expression)) {
            return defaultValue;
        }
        
        Object outcome = getELProcessor(getELProcessor).eval(toRawExpression(expression));
        if (outcome instanceof Boolean) {
            return (Boolean) outcome;
        }
        
        throw new IllegalStateException(
            "Expression " + expression + " should evaluate to boolean but evaluated to " +
             outcome == null? " null" : (outcome.getClass() + " " + outcome));
    }
    
    public static int evalImmediate(String expression, int defaultValue) {
        return evalImmediate(null, expression, defaultValue);
    }
    
    public static int evalImmediate(ELProcessor getELProcessor, String expression, int defaultValue) {
        if (!isELExpression(expression) || isDeferredExpression(expression)) {
            return defaultValue;
        }
        
        return (Integer) getELProcessor(getELProcessor).eval(toRawExpression(expression));
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T evalImmediate(String expression, T defaultValue) {
        if (!isELExpression(expression) || isDeferredExpression(expression)) {
            return defaultValue;
        }
        
        return (T) getELProcessor(getELProcessor(null)).eval(toRawExpression(expression));
    }
    
    public static String emptyIfImmediate(String expression) {
        return isImmediateExpression(expression)? "" : expression;
    }
    
    public static String evalELExpression(String expression) {
        return evalELExpression((ELProcessor)null, expression);
    }
    
    public static String evalELExpression(ELProcessor getELProcessor, String expression) {
        if (!isELExpression(expression)) {
            return expression;
        }
        
        return (String) getELProcessor(getELProcessor).eval(toRawExpression(expression));
    }
    
    public static boolean evalELExpression(String expression, boolean defaultValue) {
        return evalELExpression(null, expression, defaultValue);
    }
    
    public static boolean evalELExpression(ELProcessor getELProcessor, String expression, boolean defaultValue) {
        if (!isELExpression(expression)) {
            return defaultValue;
        }
        
        return (Boolean) getELProcessor(getELProcessor).eval(toRawExpression(expression));
    }
    
    public static <T> T evalELExpression(String expression, T defaultValue) {
        return evalELExpression(null, expression, defaultValue);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T evalELExpression(ELProcessor getELProcessor, String expression, T defaultValue) {
        if (!isELExpression(expression)) {
            return defaultValue;
        }
        
        Object outcome = getELProcessor(getELProcessor).eval(toRawExpression(expression));
        
        // Convert string representations of enums to their target, if possible
        
        // Convert single enum name to single enum
        if (defaultValue instanceof Enum  && outcome instanceof String) {
            Enum<?> defaultValueEnum = (Enum<?>) defaultValue;
            Enum<?> enumConstant = Enum.valueOf(defaultValueEnum.getClass(), (String) outcome);
            
            return (T) enumConstant;
        }
        
        // Convert single enum name to enum array (multiple enum values not supported)
        if (defaultValue instanceof Enum[]  && outcome instanceof String) {
            Enum<?>[] defaultValueEnum = (Enum<?>[]) defaultValue;
            
            @SuppressWarnings("rawtypes")
            Enum<?> enumConstant = Enum.valueOf( (Class<? extends Enum>) defaultValueEnum.getClass().getComponentType(), (String) outcome);
            
            Enum<?>[] outcomeArray = (Enum<?>[]) Array.newInstance(defaultValueEnum.getClass().getComponentType(), 1);
            outcomeArray[0] = enumConstant;
            
            return (T) outcomeArray;
        }
        
        return (T) outcome;
    }
    
    public static int evalELExpression(String expression, int defaultValue) {
        return evalELExpression(null, expression, defaultValue);
    }
    
    public static int evalELExpression(ELProcessor getELProcessor, String expression, int defaultValue) {
        if (!isELExpression(expression)) {
            return defaultValue;
        }
        
        return (Integer) getELProcessor(getELProcessor).eval(toRawExpression(expression));
    }
    
    @SafeVarargs
    public static boolean hasAnyELExpression(String... expressions) {
        return stream(expressions).anyMatch(expr -> isELExpression(expr));
    }
    
    private static boolean isELExpression(String expression) {
        return !isEmpty(expression) && (isDeferredExpression(expression) || isImmediateExpression(expression));
    }
    
    private static boolean isDeferredExpression(String expression) {
        return expression.startsWith("#{") && expression.endsWith("}");
    }
    
    private static boolean isImmediateExpression(String expression) {
        return expression.startsWith("${") && expression.endsWith("}");
    }
    
    private static String toRawExpression(String expression) {
        return expression.substring(2, expression.length() -1);
    }
}
