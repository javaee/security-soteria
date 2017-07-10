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

import javax.el.ELProcessor;

public class AnnotationELPProcessor {
    
    public static String evalImmediate(String expression) {
        if (!isELExpression(expression) || isDeferredExpression(expression)) {
            return expression;
        }
        
        return (String) getELProcessor().eval(toRawExpression(expression));
    }
    
    public static String evalELExpression(String expression) {
        if (!isELExpression(expression)) {
            return expression;
        }
        
        return (String) getELProcessor().eval(toRawExpression(expression));
    }
    
    public static String evalELExpression(ELProcessor elProcessor, String expression) {
        if (elProcessor == null || !isELExpression(expression)) {
            return expression;
        }
        
        return (String) elProcessor.eval(toRawExpression(expression));
    }
    
    @SafeVarargs
    public static boolean hasAnyELExpression(String... expressions) {
        return stream(expressions).anyMatch(expr -> isELExpression(expr));
    }
    
    private static boolean isELExpression(String expression) {
        return !isEmpty(expression) && (isDeferredExpression(expression) || isImmediateExpression(expression)) && expression.endsWith("}");
    }
    
    private static boolean isDeferredExpression(String expression) {
        return expression.startsWith("#{");
    }
    
    private static boolean isImmediateExpression(String expression) {
        return expression.startsWith("${");
    }
    
    private static String toRawExpression(String expression) {
        return expression.substring(2, expression.length() -1);
    }
}
