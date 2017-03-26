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

import java.lang.annotation.Annotation;
<<<<<<< HEAD
import org.glassfish.soteria.SecurityContextImpl;
import org.glassfish.soteria.identitystores.DataBaseIdentityStore;
import org.glassfish.soteria.identitystores.EmbeddedIdentityStore;
import org.glassfish.soteria.identitystores.LdapIdentityStore;
import org.glassfish.soteria.mechanisms.BasicAuthenticationMechanism;
import org.glassfish.soteria.mechanisms.CustomFormAuthenticationMechanism;
import org.glassfish.soteria.mechanisms.FormAuthenticationMechanism;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
=======
import static org.glassfish.soteria.cdi.CdiUtils.addAnnotatedTypes;
import static org.glassfish.soteria.cdi.CdiUtils.getAnnotation;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import javax.interceptor.Interceptor;
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
import javax.security.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.authentication.mechanism.http.annotation.AutoApplySession;
import javax.security.authentication.mechanism.http.annotation.BasicAuthenticationMechanismDefinition;
import javax.security.authentication.mechanism.http.annotation.CustomFormAuthenticationMechanismDefinition;
import javax.security.authentication.mechanism.http.annotation.FormAuthenticationMechanismDefinition;
import javax.security.authentication.mechanism.http.annotation.LoginToContinue;
import javax.security.authentication.mechanism.http.annotation.RememberMe;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.IdentityStoreHandler;
import javax.security.identitystore.annotation.DataBaseIdentityStoreDefinition;
import javax.security.identitystore.annotation.EmbeddedIdentityStoreDefinition;
import javax.security.identitystore.annotation.LdapIdentityStoreDefinition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.authentication.mechanism.http.annotation.AutoApplySession;
import javax.security.authentication.mechanism.http.annotation.LoginToContinue;
import javax.security.authentication.mechanism.http.annotation.RememberMe;

import static org.glassfish.soteria.cdi.CdiUtils.addAnnotatedTypes;
import static org.glassfish.soteria.cdi.CdiUtils.getAnnotation;

public class CdiExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(CdiExtension.class.getName());

    // Note: for now use the highlander rule: "there can be only one" for
    // authentication mechanisms.
    // This could be extended later to support multiple
    private List<Bean<IdentityStore>> identityStoreBeans = new ArrayList<>();
    private Bean<HttpAuthenticationMechanism> authenticationMechanismBean;
    private boolean httpAuthenticationMechanismFound;

    public void register(@Observes BeforeBeanDiscovery beforeBean, BeanManager beanManager) {
        addAnnotatedTypes(beforeBean, beanManager,
                AutoApplySessionInterceptor.class,
                RememberMeInterceptor.class,
                LoginToContinueInterceptor.class,
                FormAuthenticationMechanism.class,
                CustomFormAuthenticationMechanism.class,
<<<<<<< HEAD
                SecurityContextImpl.class,
                IdentityStoreHandler.class
=======
                SecurityContextImpl.class
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
        );
    }

    public <T> void processBean(@Observes ProcessBean<T> eventIn, BeanManager beanManager) {

        ProcessBean<T> event = eventIn; // JDK8 u60 workaround

<<<<<<< HEAD
        Class<?> beanClass = event.getBean().getBeanClass();
        Optional<EmbeddedIdentityStoreDefinition> optionalEmbeddedStore = getAnnotation(beanManager, event.getAnnotated(), EmbeddedIdentityStoreDefinition.class);
        if (optionalEmbeddedStore.isPresent()) {
            LOGGER.log(Level.INFO, "Activating {0} identity store from {1} class", new Object[]{EmbeddedIdentityStore.class.getName(), event.getBean().getBeanClass()});
            logActivatedIdentityStore(EmbeddedIdentityStore.class, beanClass);
            EmbeddedIdentityStoreDefinition storeDefinition = optionalEmbeddedStore.get();
            identityStoreBeans.add(new CdiProducer<IdentityStore>()
=======
        // TODO: 
        // * What if multiple definitions present?
        // *   -> Make created Bean<T>s alternatives
        // *   -> Throw exception?
        Optional<EmbeddedIdentityStoreDefinition> optionalEmbeddedStore = getAnnotation(beanManager, event.getAnnotated(), EmbeddedIdentityStoreDefinition.class);
        if (optionalEmbeddedStore.isPresent()) {
            aboutToChangeIdentityStore(EmbeddedIdentityStore.class);
            identityStoreBean = new CdiProducer<IdentityStore>()
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
                    .scope(ApplicationScoped.class)
                    .beanClass(IdentityStore.class)
                    .types(Object.class, IdentityStore.class, EmbeddedIdentityStore.class)
                    .addToId(EmbeddedIdentityStoreDefinition.class)
<<<<<<< HEAD
                    .create(e -> new EmbeddedIdentityStore(storeDefinition))
            );
=======
                    .create(e -> new EmbeddedIdentityStore(optionalEmbeddedStore.get().value()));
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
        }

        Optional<DataBaseIdentityStoreDefinition> optionalDBStore = getAnnotation(beanManager, event.getAnnotated(), DataBaseIdentityStoreDefinition.class);
        if (optionalDBStore.isPresent()) {
<<<<<<< HEAD
            logActivatedIdentityStore(DataBaseIdentityStoreDefinition.class, beanClass);
            identityStoreBeans.add(new CdiProducer<IdentityStore>()
=======
            aboutToChangeIdentityStore(DataBaseIdentityStore.class);
            identityStoreBean = new CdiProducer<IdentityStore>()
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
                    .scope(ApplicationScoped.class)
                    .beanClass(IdentityStore.class)
                    .types(Object.class, IdentityStore.class, DataBaseIdentityStore.class)
                    .addToId(DataBaseIdentityStoreDefinition.class)
<<<<<<< HEAD
                    .create(e -> new DataBaseIdentityStore(optionalDBStore.get()))
            );
=======
                    .create(e -> new DataBaseIdentityStore(optionalDBStore.get()));
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
        }

        Optional<LdapIdentityStoreDefinition> optionalLdapStore = getAnnotation(beanManager, event.getAnnotated(), LdapIdentityStoreDefinition.class);
        if (optionalLdapStore.isPresent()) {
<<<<<<< HEAD
            logActivatedIdentityStore(LdapIdentityStoreDefinition.class, beanClass);
            identityStoreBeans.add(new CdiProducer<IdentityStore>()
                    .scope(ApplicationScoped.class)
                    .beanClass(IdentityStore.class)
                    .types(Object.class, IdentityStore.class, LdapIdentityStore.class)
                    .addToId(LdapIdentityStoreDefinition.class)
                    .create(e -> new LdapIdentityStore(optionalLdapStore.get()))
            );
=======
            aboutToChangeIdentityStore(LDapIdentityStore.class);
            identityStoreBean = new CdiProducer<IdentityStore>()
                    .scope(ApplicationScoped.class)
                    .beanClass(IdentityStore.class)
                    .types(Object.class, IdentityStore.class, LDapIdentityStore.class)
                    .addToId(LdapIdentityStoreDefinition.class)
                    .create(e -> new LDapIdentityStore(optionalLdapStore.get()));
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
        }

        Optional<BasicAuthenticationMechanismDefinition> optionalBasicMechanism = getAnnotation(beanManager, event.getAnnotated(), BasicAuthenticationMechanismDefinition.class);
        if (optionalBasicMechanism.isPresent()) {
<<<<<<< HEAD
            logActivatedAuthenticationMechanism(BasicAuthenticationMechanismDefinition.class, beanClass);
=======
            aboutToChangeAuthenticationMechanism(BasicAuthenticationMechanism.class);
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
            authenticationMechanismBean = new CdiProducer<HttpAuthenticationMechanism>()
                    .scope(ApplicationScoped.class)
                    .beanClass(BasicAuthenticationMechanism.class)
                    .types(Object.class, HttpAuthenticationMechanism.class, BasicAuthenticationMechanism.class)
                    .addToId(BasicAuthenticationMechanismDefinition.class)
                    .create(e -> new BasicAuthenticationMechanism(optionalBasicMechanism.get().realmName()));
        }

        Optional<FormAuthenticationMechanismDefinition> optionalFormMechanism = getAnnotation(beanManager, event.getAnnotated(), FormAuthenticationMechanismDefinition.class);
        if (optionalFormMechanism.isPresent()) {
<<<<<<< HEAD
            logActivatedAuthenticationMechanism(FormAuthenticationMechanismDefinition.class, beanClass);
=======
            aboutToChangeAuthenticationMechanism(FormAuthenticationMechanism.class);
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
            authenticationMechanismBean = new CdiProducer<HttpAuthenticationMechanism>()
                    .scope(ApplicationScoped.class)
                    .beanClass(HttpAuthenticationMechanism.class)
                    .types(Object.class, HttpAuthenticationMechanism.class)
                    .addToId(FormAuthenticationMechanismDefinition.class)
                    .create(e -> {
                        FormAuthenticationMechanism formAuthenticationMechanism = CDI.current()
                                .select(FormAuthenticationMechanism.class)
                                .get();

                        formAuthenticationMechanism.setLoginToContinue(
                                optionalFormMechanism.get().loginToContinue());

                        return formAuthenticationMechanism;
                    });
        }

        Optional<CustomFormAuthenticationMechanismDefinition> optionalCustomFormMechanism = getAnnotation(beanManager, event.getAnnotated(), CustomFormAuthenticationMechanismDefinition.class);
        if (optionalCustomFormMechanism.isPresent()) {
<<<<<<< HEAD
            logActivatedAuthenticationMechanism(CustomFormAuthenticationMechanismDefinition.class, beanClass);
=======
            aboutToChangeAuthenticationMechanism(CustomFormAuthenticationMechanism.class);
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
            authenticationMechanismBean = new CdiProducer<HttpAuthenticationMechanism>()
                    .scope(ApplicationScoped.class)
                    .beanClass(HttpAuthenticationMechanism.class)
                    .types(Object.class, HttpAuthenticationMechanism.class)
                    .addToId(CustomFormAuthenticationMechanismDefinition.class)
                    .create(e -> {
                        CustomFormAuthenticationMechanism customFormAuthenticationMechanism = CDI.current()
                                .select(CustomFormAuthenticationMechanism.class)
                                .get();

                        customFormAuthenticationMechanism.setLoginToContinue(
                                optionalCustomFormMechanism.get().loginToContinue());

                        return customFormAuthenticationMechanism;
                    });
        }

        if (event.getBean().getTypes().contains(HttpAuthenticationMechanism.class)) {
            // enabled bean implementing the HttpAuthenticationMechanism found
            httpAuthenticationMechanismFound = true;
        }

<<<<<<< HEAD
        checkForWrongUseOfInterceptors(event.getAnnotated(), event.getBean().getBeanClass());
=======
        // Check for erroneous uses of API provided Interceptors
        checkForWrongUseOfInterceptors(event.getAnnotated(), event.getBean().getBeanClass(), AutoApplySession.class);
        checkForWrongUseOfInterceptors(event.getAnnotated(), event.getBean().getBeanClass(), LoginToContinue.class);
        checkForWrongUseOfInterceptors(event.getAnnotated(), event.getBean().getBeanClass(), RememberMe.class);
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
    }

    public void afterBean(final @Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {

<<<<<<< HEAD
        if (!identityStoreBeans.isEmpty()) {
            for (Bean<IdentityStore> identityStoreBean : identityStoreBeans) {
                afterBeanDiscovery.addBean(identityStoreBean);
            }
=======
        if (identityStoreBean != null) {
            afterBeanDiscovery.addBean(identityStoreBean);
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
        }

        if (authenticationMechanismBean != null) {
            afterBeanDiscovery.addBean(authenticationMechanismBean);
        }

        afterBeanDiscovery.addBean(
                new CdiProducer<IdentityStoreHandler>()
                .scope(ApplicationScoped.class)
                .beanClass(IdentityStoreHandler.class)
                .types(Object.class, IdentityStoreHandler.class)
                .addToId(IdentityStoreHandler.class)
                .create(e -> {
                    DefaultIdentityStoreHandler defaultIdentityStoreHandler = new DefaultIdentityStoreHandler();
                    defaultIdentityStoreHandler.init();
                    return defaultIdentityStoreHandler;
                }));
    }

    public boolean isHttpAuthenticationMechanismFound() {
        return httpAuthenticationMechanismFound;
    }

<<<<<<< HEAD
    private void logActivatedIdentityStore(Class<?> identityStoreClass, Class<?> beanClass) {
        LOGGER.log(Level.INFO, "Activating {0} identity store from {1} class", new Object[]{identityStoreClass.getName(), beanClass.getName()});
    }

    private void logActivatedAuthenticationMechanism(Class<?> identityStoreClass, Class<?> beanClass) {
        LOGGER.log(Level.INFO, "Activating {0} authentication mechanism from {1} class", new Object[]{identityStoreClass.getName(), beanClass.getName()});
    }

    private void checkForWrongUseOfInterceptors(Annotated annotated, Class<?> beanClass) {
        List<Class<? extends Annotation>> annotations = Arrays.asList(AutoApplySession.class, LoginToContinue.class, RememberMe.class);

        for (Class<? extends Annotation> annotation : annotations) {
            // Check if the class is not an interceptor, and is not a valid class to be intercepted.
            if (annotated.isAnnotationPresent(annotation)
                    && !annotated.isAnnotationPresent(javax.interceptor.Interceptor.class)
                    && !HttpAuthenticationMechanism.class.isAssignableFrom(beanClass)) {
                LOGGER.log(Level.WARNING, "Only classes implementing {0} may be annotated with {1}. {2} is annotated, but the interceptor won't take effect on it.", new Object[]{
                    HttpAuthenticationMechanism.class.getName(),
                    annotation.getName(),
                    beanClass.getName()});
            }
=======
    private void aboutToChangeIdentityStore(Class<? extends IdentityStore> newIdentityStore) {
        if (identityStoreBean == null) {
            LOGGER.log(Level.INFO, "Setting the global IdentityStore to {0}", newIdentityStore.getName());
        } else {
            LOGGER.log(Level.WARNING, "Only one IdentityStore may be enabled for at any given time. Changing the global IdentityStore from {0} to {1}", new Object[]{
                identityStoreBean.getBeanClass().getName(),
                newIdentityStore.getName()});
        }
    }

    private void aboutToChangeAuthenticationMechanism(Class<? extends HttpAuthenticationMechanism> newAuthenticationMechanism) {
        if (authenticationMechanismBean == null) {
            LOGGER.log(Level.INFO, "Setting the global HttpAuthenticationMechanism to {0}", newAuthenticationMechanism.getName());
        } else {
            LOGGER.log(Level.WARNING, "Only one HttpAuthenticationMechanism may be enabled for at any given time. Changing the global IdentityStore from {0} to {1}", new Object[]{
                authenticationMechanismBean.getBeanClass().getName(),
                newAuthenticationMechanism.getName()});
        }
    }

    private void checkForWrongUseOfInterceptors(Annotated annotated, Class<?> beanClass, Class<? extends Annotation> annotation) {
        // Check if the class is not an interceptor, and is not a valid class to be intercepted.
        if (annotated.isAnnotationPresent(annotation)
                && !annotated.isAnnotationPresent(Interceptor.class)
                && !HttpAuthenticationMechanism.class.isAssignableFrom(beanClass)) {
            LOGGER.log(Level.WARNING, "Only classes implementing {0} may be annotated with {1}. {2} is annotated, but the interceptor won't take effect on it.", new Object[]{
                HttpAuthenticationMechanism.class.getName(),
                annotation.getName(),
                beanClass.getName()});
>>>>>>> 0b4810d33627c6f2fefaa9caf9a79555456fa3fb
        }
    }
}
