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

import javax.enterprise.util.AnnotationLiteral;
import javax.security.enterprise.identitystore.IdentityStore.ValidationType;
import javax.security.enterprise.identitystore.LdapIdentityStoreDefinition;

/**
 * An annotation literal for <code>@LdapIdentityStoreDefinition</code>.
 * 
 */
@SuppressWarnings("all")
public class LdapIdentityStoreDefinitionAnnotationLiteral extends AnnotationLiteral<LdapIdentityStoreDefinition> implements LdapIdentityStoreDefinition {
    
    private static final long serialVersionUID = 1L;
    
    private final String bindDn;
    private final String bindDnPassword;
    private final String callerBaseDn;
    private final String callerNameAttribute;
    private final String callerSearchBase;
    private final String callerSearchFilter;
    private final LdapSearchScope callerSearchScope;
    private final String callerSearchScopeExpression;
    private final String groupMemberAttribute;
    private final String groupMemberOfAttribute;
    private final String groupNameAttribute;
    private final String groupSearchBase;
    private final String groupSearchFilter;
    private final LdapSearchScope groupSearchScope;
    private final String groupSearchScopeExpression;
    private final int maxResults;
    private final String maxResultsExpression;
    private final int priority;
    private final String priorityExpression;
    private final int readTimeout;
    private final String readTimeoutExpression;
    private final String url;
    private final ValidationType[] useFor;
    private final String useForExpression;
    
    private boolean hasDeferredExpressions;

    public LdapIdentityStoreDefinitionAnnotationLiteral(
            
            String bindDn,
            String bindDnPassword,
            String callerBaseDn,
            String callerNameAttribute,
            String callerSearchBase,
            String callerSearchFilter,
            LdapSearchScope callerSearchScope,
            String callerSearchScopeExpression,
            String groupMemberAttribute,
            String groupMemberOfAttribute,
            String groupNameAttribute,
            String groupSearchBase,
            String groupSearchFilter,
            LdapSearchScope groupSearchScope,
            String groupSearchScopeExpression,
            int maxResults,
            String maxResultsExpression,
            int priority,
            String priorityExpression,
            int readTimeout,
            String readTimeoutExpression,
            String url,
            ValidationType[] useFor,
            String useForExpression
            
            ) {
        
        this.bindDn = bindDn;
        this.bindDnPassword = bindDnPassword;
        this.callerBaseDn = callerBaseDn;
        this.callerNameAttribute = callerNameAttribute;
        this.callerSearchBase = callerSearchBase;
        this.callerSearchFilter = callerSearchFilter;
        this.callerSearchScope = callerSearchScope;
        this.callerSearchScopeExpression = callerSearchScopeExpression;
        this.groupMemberAttribute = groupMemberAttribute;
        this.groupMemberOfAttribute = groupMemberOfAttribute;
        this.groupNameAttribute = groupMemberOfAttribute;
        this.groupSearchBase = groupSearchBase;
        this.groupSearchFilter = groupSearchFilter;
        this.groupSearchScope = groupSearchScope;
        this.groupSearchScopeExpression = groupSearchScopeExpression;
        this.maxResults = maxResults;
        this.maxResultsExpression = maxResultsExpression;
        this.priority = priority;
        this.priorityExpression = priorityExpression;
        this.readTimeout = readTimeout;
        this.readTimeoutExpression = readTimeoutExpression;
        this.url = url;
        this.useFor = useFor;
        this.useForExpression = useForExpression;

    }
    
    public static LdapIdentityStoreDefinition eval(LdapIdentityStoreDefinition in) {
        if (!hasAnyELExpression(in)) {
            return in;
        }
        
        try {
            LdapIdentityStoreDefinitionAnnotationLiteral out =
                new LdapIdentityStoreDefinitionAnnotationLiteral(
                    in.bindDn(),
                    in.bindDnPassword(),
                    in.callerBaseDn(),
                    in.callerNameAttribute(),
                    in.callerSearchBase(),
                    in.callerSearchFilter(),
                    in.callerSearchScope(),
                    in.callerSearchScopeExpression(),
                    in.groupMemberAttribute(),
                    in.groupMemberOfAttribute(),
                    in.groupNameAttribute(),
                    in.groupSearchBase(),
                    in.groupSearchFilter(),
                    in.groupSearchScope(),
                    in.groupSearchScopeExpression(),
                    in.maxResults(),
                    in.maxResultsExpression(),
                    in.priority(),
                    in.priorityExpression(),
                    in.readTimeout(),
                    in.readTimeoutExpression(),
                    in.url(),
                    in.useFor(),
                    in.useForExpression()
                );
            
            out.setHasDeferredExpressions(hasAnyELExpression(out));
            
            return out;
        } catch (Throwable t) {
            t.printStackTrace();
            
            throw t;
        }
    }
    
    public static boolean hasAnyELExpression(LdapIdentityStoreDefinition in) {
        return AnnotationELPProcessor.hasAnyELExpression(
            in.bindDn(),
            in.bindDnPassword(),
            in.callerNameAttribute(),
            in.callerSearchBase(),
            in.callerSearchFilter(),
            in.callerSearchScopeExpression(),
            in.groupMemberAttribute(),
            in.groupMemberOfAttribute(),
            in.groupNameAttribute(),
            in.groupSearchBase(),
            in.groupSearchFilter(),
            in.groupSearchScopeExpression(),
            in.maxResultsExpression(),
            in.priorityExpression(),
            in.readTimeoutExpression(),
            in.url(),
            in.useForExpression()
        );
    }
    
    @Override
    public String bindDn() {
        return hasDeferredExpressions? evalELExpression(bindDn) : bindDn;
    }
    
    @Override
    public String bindDnPassword() {
        return hasDeferredExpressions? evalELExpression(bindDnPassword) : bindDnPassword;
    }
    
    @Override
    public String callerBaseDn() {
        return hasDeferredExpressions? evalELExpression(callerBaseDn) : callerBaseDn;
    }
    
    @Override
    public String callerNameAttribute() {
        return hasDeferredExpressions? evalELExpression(callerNameAttribute) : callerNameAttribute;
    }
    
    @Override
    public String callerSearchBase() {
        return hasDeferredExpressions? evalELExpression(callerSearchBase) : callerSearchBase;
    }
    
    @Override
    public String callerSearchFilter() {
        return hasDeferredExpressions? evalELExpression(callerSearchFilter) : callerSearchFilter;
    }
    
    @Override
    public LdapSearchScope callerSearchScope() {
        return hasDeferredExpressions? evalELExpression(callerSearchScopeExpression, callerSearchScope) : callerSearchScope;
    }
    
    @Override
    public String callerSearchScopeExpression() {
        return hasDeferredExpressions? evalELExpression(callerSearchScopeExpression) : callerSearchScopeExpression;
    }
    
    @Override
    public String groupMemberAttribute() {
        return hasDeferredExpressions? evalELExpression(groupMemberAttribute) : groupMemberAttribute;
    }
    
    @Override
    public String groupMemberOfAttribute() {
        return hasDeferredExpressions? evalELExpression(groupMemberOfAttribute) : groupMemberOfAttribute;
    }
    
    @Override
    public String groupNameAttribute() {
        return hasDeferredExpressions? evalELExpression(groupNameAttribute) : groupNameAttribute;
    }
    
    @Override
    public String groupSearchBase() {
        return hasDeferredExpressions? evalELExpression(groupSearchBase) : groupSearchBase;
    }
    
    @Override
    public String groupSearchFilter() {
        return hasDeferredExpressions? evalELExpression(groupSearchFilter) : groupSearchFilter;
    }
    
    @Override
    public LdapSearchScope groupSearchScope() {
        return hasDeferredExpressions? evalELExpression(groupSearchScopeExpression, groupSearchScope) : groupSearchScope;
    }
    
    @Override
    public String groupSearchScopeExpression() {
        return groupSearchScopeExpression;
    }
    
    @Override
    public int maxResults() {
        return hasDeferredExpressions? evalELExpression(maxResultsExpression, maxResults) : maxResults;
    }
    
    @Override
    public String maxResultsExpression() {
        return maxResultsExpression;
    }
    
    @Override
    public int priority() {
        return hasDeferredExpressions? evalELExpression(priorityExpression, priority) : priority;
    }
    
    @Override
    public String priorityExpression() {
        return priorityExpression;
    }
    
    @Override
    public int readTimeout() {
        return hasDeferredExpressions? evalELExpression(readTimeoutExpression, readTimeout) : readTimeout;
    }
    
    @Override
    public String readTimeoutExpression() {
        return readTimeoutExpression;
    }
    
    @Override
    public String url() {
        return url;
    }
    
    @Override
    public ValidationType[] useFor() {
        return hasDeferredExpressions? evalELExpression(useForExpression, useFor) : useFor;
    }
    
    @Override
    public String useForExpression() {
        return useForExpression;
    }
    
    public boolean isHasDeferredExpressions() {
        return hasDeferredExpressions;
    }

    public void setHasDeferredExpressions(boolean hasDeferredExpressions) {
        this.hasDeferredExpressions = hasDeferredExpressions;
    }
}
