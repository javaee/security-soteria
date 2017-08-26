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

import javax.enterprise.util.AnnotationLiteral;
import javax.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import javax.security.enterprise.identitystore.PasswordHash;
import javax.security.enterprise.identitystore.IdentityStore.ValidationType;

/**
 * An annotation literal for <code>@DatabaseIdentityStoreDefinition</code>.
 * 
 */
@SuppressWarnings("all")
public class DatabaseIdentityStoreDefinitionAnnotationLiteral extends AnnotationLiteral<DatabaseIdentityStoreDefinition> implements DatabaseIdentityStoreDefinition {
    
    private static final long serialVersionUID = 1L;
    
    private final String dataSourceLookup;
    private final String callerQuery;
    private final String groupsQuery;
    private final Class<? extends PasswordHash> hashAlgorithm;
    private final String[] hashAlgorithmParameters;
    private final int priority;
    private final String priorityExpression;
    private final ValidationType[] useFor;
    private final String useForExpression;
    
    private boolean hasDeferredExpressions;

    public DatabaseIdentityStoreDefinitionAnnotationLiteral(
            
        String dataSourceLookup, 
        String callerQuery, 
        String groupsQuery, 
        Class<? extends PasswordHash> hashAlgorithm,
        String[] hashAlgorithmParameters,
        int priority,
        String priorityExpression,
        ValidationType[] useFor,
        String useForExpression
        
            ) {
        
        this.dataSourceLookup = dataSourceLookup;
        this.callerQuery = callerQuery;
        this.groupsQuery = groupsQuery;
        this.hashAlgorithm = hashAlgorithm;
        this.hashAlgorithmParameters = hashAlgorithmParameters;
        this.priority = priority;
        this.priorityExpression = priorityExpression;
        this.useFor = useFor;
        this.useForExpression = useForExpression;
    }
    
    public static DatabaseIdentityStoreDefinition eval(DatabaseIdentityStoreDefinition in) {
        if (!hasAnyELExpression(in)) {
            return in;
        }
        
        DatabaseIdentityStoreDefinitionAnnotationLiteral out = new DatabaseIdentityStoreDefinitionAnnotationLiteral(
            evalImmediate(in.dataSourceLookup()),
            evalImmediate(in.callerQuery()), 
            evalImmediate(in.groupsQuery()), 
            in.hashAlgorithm(),
            in.hashAlgorithmParameters(),
            evalImmediate(in.priorityExpression(), in.priority()),
            emptyIfImmediate(in.priorityExpression()),
            evalImmediate(in.useForExpression(), in.useFor()),
            emptyIfImmediate(in.useForExpression())
        );
        
        out.setHasDeferredExpressions(hasAnyELExpression(out));
        
        return out;
    }
    
    public static boolean hasAnyELExpression(DatabaseIdentityStoreDefinition in) {
        return AnnotationELPProcessor.hasAnyELExpression(
            in.dataSourceLookup(),
            in.callerQuery(), 
            in.groupsQuery(), 
            in.priorityExpression(),
            in.useForExpression()
       );
    }
    
    @Override
    public String dataSourceLookup() {
        return hasDeferredExpressions? evalELExpression(dataSourceLookup) : dataSourceLookup;
    }
    
    @Override
    public String callerQuery() {
        return hasDeferredExpressions? evalELExpression(callerQuery) : callerQuery;
    }
    
    @Override
    public String groupsQuery() {
        return hasDeferredExpressions? evalELExpression(groupsQuery) : groupsQuery;
    }
    
    @Override
    public Class<? extends PasswordHash> hashAlgorithm() {
        return hashAlgorithm;
    }
    
    @Override
    public String[] hashAlgorithmParameters() {
        return hashAlgorithmParameters;
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
