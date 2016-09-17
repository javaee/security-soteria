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

import org.glassfish.soteria.SecurityContextImpl;
import org.glassfish.soteria.identitystores.DataBaseIdentityStore;
import org.glassfish.soteria.identitystores.EmbeddedIdentityStore;
import org.glassfish.soteria.identitystores.LDapIdentityStore;
import org.glassfish.soteria.mechanisms.BasicAuthenticationMechanism;
import org.glassfish.soteria.mechanisms.CustomFormAuthenticationMechanism;
import org.glassfish.soteria.mechanisms.FormAuthenticationMechanism;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.security.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.authentication.mechanism.http.annotation.BasicAuthenticationMechanismDefinition;
import javax.security.authentication.mechanism.http.annotation.CustomFormAuthenticationMechanismDefinition;
import javax.security.authentication.mechanism.http.annotation.FormAuthenticationMechanismDefinition;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.IdentityStoreHandler;
import javax.security.identitystore.annotation.DataBaseIdentityStoreDefinition;
import javax.security.identitystore.annotation.EmbeddedIdentityStoreDefinition;
import javax.security.identitystore.annotation.LdapIdentityStoreDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.glassfish.soteria.cdi.CdiUtils.addAnnotatedTypes;
import static org.glassfish.soteria.cdi.CdiUtils.getAnnotation;

public class CdiExtension implements Extension {

    // Note: for now use the highlander rule: "there can be only one" for
    // both identity stores and (http) authentication mechanisms.
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
                SecurityContextImpl.class,
                IdentityStoreHandler.class
        );
    }

    public <T> void processBean(@Observes ProcessBean<T> eventIn, BeanManager beanManager) {

        ProcessBean<T> event = eventIn; // JDK8 u60 workaround

        Optional<EmbeddedIdentityStoreDefinition> optionalEmbeddedStore = getAnnotation(beanManager, event.getAnnotated(), EmbeddedIdentityStoreDefinition.class);
        if (optionalEmbeddedStore.isPresent()) {
            EmbeddedIdentityStoreDefinition storeDefinition = optionalEmbeddedStore.get();
            identityStoreBeans.add(new CdiProducer<IdentityStore>()
                    .scope(ApplicationScoped.class)
                    .beanClass(IdentityStore.class)
                    .types(Object.class, IdentityStore.class, EmbeddedIdentityStore.class)
                    .addToId(EmbeddedIdentityStoreDefinition.class)
                    .create(e -> new EmbeddedIdentityStore(storeDefinition))
            );
        }

        Optional<DataBaseIdentityStoreDefinition> optionalDBStore = getAnnotation(beanManager, event.getAnnotated(), DataBaseIdentityStoreDefinition.class);
        if (optionalDBStore.isPresent()) {
            identityStoreBeans.add(new CdiProducer<IdentityStore>()
                    .scope(ApplicationScoped.class)
                    .beanClass(IdentityStore.class)
                    .types(Object.class, IdentityStore.class, DataBaseIdentityStore.class)
                    .addToId(DataBaseIdentityStoreDefinition.class)
                    .create(e -> new DataBaseIdentityStore(optionalDBStore.get()))
            );
        }

        Optional<LdapIdentityStoreDefinition> optionalLdapStore = getAnnotation(beanManager, event.getAnnotated(), LdapIdentityStoreDefinition.class);
        if (optionalLdapStore.isPresent()) {
            identityStoreBeans.add(new CdiProducer<IdentityStore>()
                    .scope(ApplicationScoped.class)
                    .beanClass(IdentityStore.class)
                    .types(Object.class, IdentityStore.class, LDapIdentityStore.class)
                    .addToId(LdapIdentityStoreDefinition.class)
                    .create(e -> new LDapIdentityStore(optionalLdapStore.get()))
            );
        }

        Optional<BasicAuthenticationMechanismDefinition> optionalBasicMechanism = getAnnotation(beanManager, event.getAnnotated(), BasicAuthenticationMechanismDefinition.class);
        if (optionalBasicMechanism.isPresent()) {
            authenticationMechanismBean = new CdiProducer<HttpAuthenticationMechanism>()
                    .scope(ApplicationScoped.class)
                    .beanClass(BasicAuthenticationMechanism.class)
                    .types(Object.class, HttpAuthenticationMechanism.class, BasicAuthenticationMechanism.class)
                    .addToId(BasicAuthenticationMechanismDefinition.class)
                    .create(e -> new BasicAuthenticationMechanism(optionalBasicMechanism.get().realmName()));
        }

        Optional<FormAuthenticationMechanismDefinition> optionalFormMechanism = getAnnotation(beanManager, event.getAnnotated(), FormAuthenticationMechanismDefinition.class);
        if (optionalFormMechanism.isPresent()) {
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

        if (event.getBean().getBeanClass().isAssignableFrom(IdentityStoreHandler.class)) {
            System.out.println(event);
        }
        if (event.getBean().getTypes().contains(HttpAuthenticationMechanism.class)) {
            // enabled bean implementing the HttpAuthenticationMechanism found
            httpAuthenticationMechanismFound = true;
        }

    }

    public void afterBean(final @Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {

        if (!identityStoreBeans.isEmpty()) {
            for (Bean<IdentityStore> identityStoreBean : identityStoreBeans) {
                afterBeanDiscovery.addBean(identityStoreBean);
            }
        }

        if (authenticationMechanismBean != null) {
            afterBeanDiscovery.addBean(authenticationMechanismBean);
        }

        // FIXME Since we have no beans.xml in this jar (and apparently we can't have one) we have to solve it this way.
        // Needs to be a regular CDI bean because we need to be able to @Specialize it.
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

}
