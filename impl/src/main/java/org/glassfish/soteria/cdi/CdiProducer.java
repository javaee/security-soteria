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
import static java.util.Collections.unmodifiableSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;

// May be replaced by CDI 2.0 bean builder API when ready.
// See http://weld.cdi-spec.org/news/2015/02/25/weld-300Alpha5/#_bean_builder_api
public class CdiProducer<T> implements Bean<T>, PassivationCapable {
    
    private String id = this.getClass().getName();
    private String name;
    private Class<?> beanClass = Object.class;
    private Set<Type> types = singleton(Object.class);
    private Set<Annotation> qualifiers = unmodifiableSet(asSet(new DefaultAnnotationLiteral(), new AnyAnnotationLiteral()));
    private Class<? extends Annotation> scope = Dependent.class;
    private Function<CreationalContext<T>, T> create;
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Class<?> getBeanClass() {
       return beanClass;
    }
    
    @Override
    public Set<Type> getTypes() {
        return types;
    }
    
    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }
    
    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }
    
    @Override
    public T create(CreationalContext<T> creationalContext) {
        return create.apply(creationalContext);
    }
    
    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
    }
    
    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return emptySet();
    }
    
    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return emptySet();
    }
    
    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return false;
    }
    
    protected CdiProducer<T> active(boolean active) {
        return this;
    }
    
    protected CdiProducer<T> name(String name) {
        this.name = name;
        return this;
    }
    
    protected CdiProducer<T> create(Function<CreationalContext<T>, T> create) {
        this.create = create;
        return this;
    }
    
    protected CdiProducer<T> beanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
        return this;
    }
    
    protected CdiProducer<T> types(Type... types) {
        this.types = asSet(types);
        return this;
    }
    
    protected CdiProducer<T> beanClassAndType(Class<?> beanClass) {
        beanClass(beanClass);
        types(beanClass);
        return this;
    }
    
    protected CdiProducer<T> qualifiers(Annotation... qualifiers) {
        this.qualifiers = asSet(qualifiers);
        return this;
    }
    
    
    protected CdiProducer<T> scope(Class<? extends Annotation> scope) {
        this.scope = scope;
        return this;
    }
    
    protected CdiProducer<T> addToId(Object object) {
        id = id + " " + object.toString();
        return this;
    }
    
    @SafeVarargs
    protected static <T> Set<T> asSet(T... a) {
        return new HashSet<T>(asList(a));
    }
    
}
