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
package org.glassfish.soteria.cdi;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.security.authentication.mechanism.http.HttpAuthenticationMechanism;

// WIP class - can likely be removed
public class DynamicHttpAuthenticationDecorator implements Decorator<HttpAuthenticationBaseDecorator> {

    private final Set<Type> types = new HashSet<Type>(asList(HttpAuthenticationBaseDecorator.class, Object.class));
    private final Set<Type> decoratedTypes = singleton(HttpAuthenticationMechanism.class);
    
    private final BeanManager beanManager;
    private final InjectionPoint decoratorInjectionPoint;
    private final Set<InjectionPoint> injectionPoints;

    public DynamicHttpAuthenticationDecorator(BeanManager beanManager) {
        
        decoratorInjectionPoint = new DecoratorInjectionPoint(
            HttpAuthenticationMechanism.class, 
            beanManager.createAnnotatedType(HttpAuthenticationBaseDecorator.class).getFields().iterator().next(), 
            this);
        
        injectionPoints = singleton(decoratorInjectionPoint);
        
        this.beanManager = beanManager;
    }

    public HttpAuthenticationBaseDecorator create(CreationalContext<HttpAuthenticationBaseDecorator> creationalContext) {
        return new AutoApplySessionDecorator(
            (HttpAuthenticationMechanism) beanManager.getInjectableReference(decoratorInjectionPoint, creationalContext));
    }

    public void destroy(HttpAuthenticationBaseDecorator instance, CreationalContext<HttpAuthenticationBaseDecorator> creationalContext) {
        creationalContext.release();
    }
    
    public Set<Type> getTypes() {
        return types;
    }
    
    public Set<Type> getDecoratedTypes() {
        return decoratedTypes;
    }
    
    public Class<?> getBeanClass() {
        return HttpAuthenticationBaseDecorator.class;
    }

    public Type getDelegateType() {
        return HttpAuthenticationMechanism.class;
    }

    public Set<InjectionPoint> getInjectionPoints() {
        return injectionPoints;
    }
    
    public Set<Annotation> getDelegateQualifiers() {
        return emptySet();
    }

    public String getName() {
        return null;
    }

    public Set<Annotation> getQualifiers() {
        return emptySet();
    }

    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    public Set<Class<? extends Annotation>> getStereotypes() {
        return emptySet();
    }

    public boolean isAlternative() {
        return false;
    }

    public boolean isNullable() {
        return false;
    }

    private static class DecoratorInjectionPoint implements InjectionPoint {
        
        private final Set<Annotation> qualifiers = singleton(new DefaultAnnotationLiteral());
        
        private final Type type; 
        private final AnnotatedField<?> annotatedField; 
        private final Bean<?> bean;

        public DecoratorInjectionPoint(Type type, AnnotatedField<?> annotatedField, Bean<?> bean) {
            this.type = type;
            this.annotatedField = annotatedField;
            this.bean = bean;
        }
        
        public Type getType() {
            return type;
        }
        
        public Set<Annotation> getQualifiers() {
            return qualifiers;
        }
        
        public Bean<?> getBean() {
            return bean;
        }
        
        public Member getMember() {
            return annotatedField.getJavaMember();
        }
        
        public Annotated getAnnotated() {
            return annotatedField;
        }
        
        public boolean isDelegate() {
            return true;
        }

        
        public boolean isTransient() {
            return false;
        }
      
    };
}