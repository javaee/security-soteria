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

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.BiFunction;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

// WIP - Builder for dynanic decorators. May not be needed and/or replaced by
// CDI 2.0 bean builder
// See http://weld.cdi-spec.org/news/2015/02/25/weld-300Alpha5/#_bean_builder_api
public class CdiDecorator<T> extends CdiProducer<T> implements Decorator<T> {
    
    private Class<T> decorator;
    private Type delegateType;
    private Set<Type> decoratedTypes;
    
    private BeanManager beanManager;
    private InjectionPoint decoratorInjectionPoint;
    private Set<InjectionPoint> injectionPoints;
    
    private BiFunction<CreationalContext<T>, Object, T> create;
    
    
    @Override
    public T create(CreationalContext<T> creationalContext) {
        return create.apply(creationalContext, beanManager.getInjectableReference(decoratorInjectionPoint, creationalContext));
    }
    
    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return injectionPoints;
    }

    public Type getDelegateType() {
        return delegateType;
    }
 
    public Set<Type> getDecoratedTypes() {
        return decoratedTypes;
    }
    
    public Set<Annotation> getDelegateQualifiers() {
        return emptySet();
    }
    
    public CdiDecorator<T> decorator(Class<T> decorator) {
        this.decorator = decorator;
        beanClassAndType(decorator);
        return this;
    }
    
    public CdiDecorator<T> delegateAndDecoratedType(Type type) {
        delegateType = type;
        decoratedTypes = asSet(type);
        return this;
    }
    
    public CdiProducer<T> create(BeanManager beanManager, BiFunction<CreationalContext<T>, Object, T> create) {
        
        decoratorInjectionPoint = new DecoratorInjectionPoint(
                getDelegateType(), 
                beanManager.createAnnotatedType(decorator).getFields().iterator().next(), 
                this);
            
            injectionPoints = singleton(decoratorInjectionPoint);
            
            this.beanManager = beanManager;
        
        this.create = create;
        return this;
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
