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

import static org.glassfish.soteria.cdi.AnnotationELPProcessor.evalELExpression;
import static org.glassfish.soteria.cdi.AnnotationELPProcessor.evalImmediate;

import javax.enterprise.util.AnnotationLiteral;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;

/**
 * An annotation literal for <code>@LoginToContinue</code>.
 * 
 */
@SuppressWarnings("all")
public class LoginToContinueAnnotationLiteral extends AnnotationLiteral<LoginToContinue> implements LoginToContinue {
    
    private static final long serialVersionUID = 1L;

    private final String loginPage;
    private final boolean useForwardToLogin;
    private final String useForwardToLoginExpression;
    private final String errorPage;
    
    private boolean hasDeferredExpressions;

    public LoginToContinueAnnotationLiteral(String loginPage, boolean useForwardToLogin, String useForwardToLoginExpression, String errorPage) {
        this.loginPage = loginPage;
        this.useForwardToLogin = useForwardToLogin;
        this.useForwardToLoginExpression = useForwardToLoginExpression;
        this.errorPage = errorPage;
    }
    
    public static LoginToContinue eval(LoginToContinue in) {
        if (!hasAnyELExpression(in)) {
            return in;
        }
        
        LoginToContinueAnnotationLiteral out =
            new LoginToContinueAnnotationLiteral(
                    evalImmediate(in.loginPage()), 
                    in.useForwardToLogin(), 
                    evalImmediate(in.useForwardToLoginExpression()), 
                    evalImmediate(in.errorPage())
            );
        
        out.setHasDeferredExpressions(hasAnyELExpression(out));
        
        return out;
    }
    
    public static boolean hasAnyELExpression(LoginToContinue in) {
        return AnnotationELPProcessor.hasAnyELExpression(
            in.loginPage(), 
            in.errorPage(),
            in.useForwardToLoginExpression()
        );
    }

    @Override
    public String loginPage() {
        return hasDeferredExpressions? evalELExpression(loginPage) : loginPage;
    }

    @Override
    public boolean useForwardToLogin() {
        return useForwardToLogin;
    }
    
    @Override
    public String useForwardToLoginExpression() {
        return hasDeferredExpressions? evalELExpression(useForwardToLoginExpression) : useForwardToLoginExpression;
    }

    @Override
    public String errorPage() {
        return hasDeferredExpressions? evalELExpression(errorPage) : errorPage;
    }
    
    public boolean isHasDeferredExpressions() {
        return hasDeferredExpressions;
    }

    public void setHasDeferredExpressions(boolean hasDeferredExpressions) {
        this.hasDeferredExpressions = hasDeferredExpressions;
    }
}
