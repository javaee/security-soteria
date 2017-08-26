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

package org.glassfish.soteria.cdi;

import static org.glassfish.soteria.cdi.AnnotationELPProcessor.emptyIfImmediate;
import static org.glassfish.soteria.cdi.AnnotationELPProcessor.evalELExpression;
import static org.glassfish.soteria.cdi.AnnotationELPProcessor.evalImmediate;

import javax.el.ELProcessor;
import javax.enterprise.util.AnnotationLiteral;
import javax.security.enterprise.authentication.mechanism.http.RememberMe;

/**
 * An annotation literal for <code>@RememberMe</code>.
 * 
 */
@SuppressWarnings("all")
public class RememberMeAnnotationLiteral extends AnnotationLiteral<RememberMe> implements RememberMe {
    
    private static final long serialVersionUID = 1L;
    
    private final int cookieMaxAgeSeconds;
    private final String cookieMaxAgeSecondsExpression;
    private final boolean cookieSecureOnly;
    private final String cookieSecureOnlyExpression;
    private final boolean cookieHttpOnly;
    private final String cookieHttpOnlyExpression;
    private final String cookieName;
    private final boolean isRememberMe;
    private final String isRememberMeExpression;
    
    private final ELProcessor elProcessor;
    
    private boolean hasDeferredExpressions;

    public RememberMeAnnotationLiteral(
        
        int cookieMaxAgeSeconds,
        String cookieMaxAgeSecondsExpression,
        boolean cookieSecureOnly,
        String cookieSecureOnlyExpression,
        boolean cookieHttpOnly,
        String cookieHttpOnlyExpression,
        String cookieName,
        boolean isRememberMe,
        String isRememberMeExpression,
        ELProcessor elProcessor
        
            ) {
        
        this.cookieMaxAgeSeconds = cookieMaxAgeSeconds;
        this.cookieMaxAgeSecondsExpression = cookieMaxAgeSecondsExpression;
        this.cookieSecureOnly = cookieSecureOnly;
        this.cookieSecureOnlyExpression = cookieSecureOnlyExpression;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieHttpOnlyExpression = cookieHttpOnlyExpression;
        this.cookieName = cookieName;
        this.isRememberMe = isRememberMe;
        this.isRememberMeExpression = isRememberMeExpression;
        this.elProcessor = elProcessor;
        
    }
    
    public static RememberMe eval(RememberMe in, ELProcessor elProcessor) {
        if (!hasAnyELExpression(in)) {
            return in;
        }
        
        try {
            RememberMeAnnotationLiteral out =
                new RememberMeAnnotationLiteral(
                    evalImmediate(elProcessor, in.cookieMaxAgeSecondsExpression(), in.cookieMaxAgeSeconds()), 
                    emptyIfImmediate(in.cookieMaxAgeSecondsExpression()),
                    evalImmediate(elProcessor, in.cookieSecureOnlyExpression(), in.cookieSecureOnly()),
                    emptyIfImmediate(in.cookieSecureOnlyExpression()),
                    evalImmediate(elProcessor, in.cookieHttpOnlyExpression(), in.cookieHttpOnly()),
                    emptyIfImmediate(in.cookieHttpOnlyExpression()),
                    evalImmediate(elProcessor, in.cookieName()),
                    evalImmediate(elProcessor, in.isRememberMeExpression(), in.isRememberMe()),
                    evalImmediate(elProcessor, in.isRememberMeExpression()),
                    elProcessor
                );
        
            out.setHasDeferredExpressions(hasAnyELExpression(out));
        
            return out;
        } catch (Throwable t) {
            t.printStackTrace();
            
            throw t;
        }
    }
    
    public static boolean hasAnyELExpression(RememberMe in) {
        return AnnotationELPProcessor.hasAnyELExpression(
            in.cookieMaxAgeSecondsExpression(),
            in.cookieSecureOnlyExpression(),
            in.cookieHttpOnlyExpression(),
            in.cookieName(),
            in.isRememberMeExpression()
        );
    }
    
    @Override
    public boolean cookieHttpOnly() {
        return hasDeferredExpressions? evalELExpression(elProcessor, cookieHttpOnlyExpression, cookieHttpOnly) : cookieHttpOnly;
    }
    
    @Override
    public String cookieHttpOnlyExpression() {
        return cookieHttpOnlyExpression;
    }
    
    @Override
    public int cookieMaxAgeSeconds() {
        return hasDeferredExpressions? evalELExpression(elProcessor, cookieMaxAgeSecondsExpression, cookieMaxAgeSeconds) : cookieMaxAgeSeconds;
    }
    
    @Override
    public String cookieMaxAgeSecondsExpression() {
        return cookieMaxAgeSecondsExpression;
    }

    @Override
    public boolean cookieSecureOnly() {
        return hasDeferredExpressions? evalELExpression(elProcessor, cookieSecureOnlyExpression, cookieSecureOnly) : cookieSecureOnly;
    }

    @Override
    public String cookieSecureOnlyExpression() {
        return cookieSecureOnlyExpression;
    }

    @Override
    public String cookieName() {
        return hasDeferredExpressions? evalELExpression(elProcessor, cookieName) : cookieName;
    }
    
    @Override
    public boolean isRememberMe() {
        return hasDeferredExpressions? evalELExpression(elProcessor, isRememberMeExpression, isRememberMe) : isRememberMe;
    }

    @Override
    public String isRememberMeExpression() {
        return isRememberMeExpression;
    }
    
    public boolean isHasDeferredExpressions() {
        return hasDeferredExpressions;
    }

    public void setHasDeferredExpressions(boolean hasDeferredExpressions) {
        this.hasDeferredExpressions = hasDeferredExpressions;
    }
}
