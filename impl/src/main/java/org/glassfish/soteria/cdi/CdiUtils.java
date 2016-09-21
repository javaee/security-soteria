/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
import static java.util.Optional.empty;
import static org.glassfish.soteria.Utils.isEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

public class CdiUtils {
	
    public static <A extends Annotation> Optional<A> getAnnotation(BeanManager beanManager, Annotated annotated, Class<A> annotationType) {

        annotated.getAnnotation(annotationType);

        if (annotated.getAnnotations().isEmpty()) {
            return empty();
        }

        if (annotated.isAnnotationPresent(annotationType)) {
            return Optional.of(annotated.getAnnotation(annotationType));
        }

        Queue<Annotation> annotations = new LinkedList<>(annotated.getAnnotations());

        while (!annotations.isEmpty()) {
            Annotation annotation = annotations.remove();

            if (annotation.annotationType().equals(annotationType)) {
                return Optional.of(annotationType.cast(annotation));
            }

            if (beanManager.isStereotype(annotation.annotationType())) {
                annotations.addAll(
                    beanManager.getStereotypeDefinition(
                        annotation.annotationType()
                    )
                );
            }
        }

        return empty();
    }
    
    public static void addAnnotatedTypes(BeforeBeanDiscovery beforeBean, BeanManager beanManager, Class<?>... types) {
        for (Class<?> type : types) {
            beforeBean.addAnnotatedType(beanManager.createAnnotatedType(type), "Soteria " + type.getName());
        }
    }
    
    public static <A extends Annotation> Optional<A> getAnnotation(BeanManager beanManager, Class<?> annotatedClass, Class<A> annotationType) {

        if (annotatedClass.isAnnotationPresent(annotationType)) {
            return Optional.of(annotatedClass.getAnnotation(annotationType));
        }

        Queue<Annotation> annotations = new LinkedList<>(asList(annotatedClass.getAnnotations()));

        while (!annotations.isEmpty()) {
            Annotation annotation = annotations.remove();

            if (annotation.annotationType().equals(annotationType)) {
                return Optional.of(annotationType.cast(annotation));
            }

            if (beanManager.isStereotype(annotation.annotationType())) {
                annotations.addAll(
                    beanManager.getStereotypeDefinition(
                        annotation.annotationType()
                    )
                );
            }
        }

        return empty();
    }
    
    // 
    public static <T> T getBeanReference(Class<T> type, Annotation... qualifiers) {
        return type.cast(getBeanReferenceByType(jndiLookup("java:comp/BeanManager"), type, qualifiers));
    }
    
    /**
     * @param beanManager the bean manager
     * @param type the required bean type the reference must have
     * @param qualifiers the required qualifiers the reference must have
     * @return a bean reference adhering to the required type and qualifiers
     */
    public static <T> T getBeanReference(BeanManager beanManager, Class<T> type, Annotation... qualifiers) {
        return type.cast(getBeanReferenceByType(beanManager, type, qualifiers));
    }

    public static Object getBeanReferenceByType(BeanManager beanManager, Type type, Annotation... qualifiers) {

        Object beanReference = null;

        Bean<?> bean = beanManager.resolve(beanManager.getBeans(type, qualifiers));
        if (bean != null) {
            beanReference = beanManager.getReference(bean, type, beanManager.createCreationalContext(bean));
        }

        return beanReference;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T getContextualReference(Class<T> type, BeanManager beanManager, Set<Bean<?>> beans) {
        
        Object beanReference = null;
        
        Bean<?> bean = beanManager.resolve(beans);
        if (bean != null) {
            beanReference = beanManager.getReference(bean, type, beanManager.createCreationalContext(bean));
        }
        
        return (T) beanReference;
    }

    public static <T> List<T> getBeanReferencesByType(Class<T> type, boolean optional) {
        BeanManager beanManager =  jndiLookup("java:comp/BeanManager");

        Set<Bean<?>> beans = getBeanDefinitions(type, optional, beanManager);

        List<T> result = new ArrayList<>(beans.size());

        for (Bean<?> bean : beans) {
            result.add(getContextualReference(type, beanManager, Collections.<Bean<?>>singleton(bean)));
        }

        return result;
    }

    private static <T> Set<Bean<?>> getBeanDefinitions(Class<T> type, boolean optional, BeanManager beanManager) {
        Set<Bean<?>> beans = beanManager.getBeans(type, new AnyAnnotationLiteral());
        if (!isEmpty(beans)) {
            return beans;
        } 
        
        if (optional) {
            return emptySet();
        } 
        
        throw new IllegalStateException("Could not find beans for Type=" + type);
    }

    @SuppressWarnings("unchecked")
    public static <T> T jndiLookup(String name) {
        InitialContext context = null;
        try {
            context = new InitialContext();
            return (T) context.lookup(name);
        } catch (NamingException e) {
            if (is(e, NameNotFoundException.class)) {
                return null;
            } else {
                throw new IllegalStateException(e);
            }
        } finally {
            close(context);
        }
    }

    private static void close(InitialContext context) {
        try {
            if (context != null) {
                context.close();
            }
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public static <T extends Throwable> boolean is(Throwable exception, Class<T> type) {
        Throwable unwrappedException = exception;

        while (unwrappedException != null) {
            if (type.isInstance(unwrappedException)) {
                return true;
            }

            unwrappedException = unwrappedException.getCause();
        }

        return false;
    }

}
