package org.glassfish.soteria.cdi;

import static org.glassfish.soteria.cdi.CdiUtils.addAnnotatedTypes;
import static org.glassfish.soteria.cdi.CdiUtils.getAnnotation;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import javax.security.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.authentication.mechanism.http.annotation.BasicAuthenticationMechanismDefinition;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.annotation.DataBaseIdentityStoreDefinition;
import javax.security.identitystore.annotation.EmbeddedIdentityStoreDefinition;
import javax.security.identitystore.annotation.LdapIdentityStoreDefinition;

import org.glassfish.soteria.identitystores.DataBaseIdentityStore;
import org.glassfish.soteria.identitystores.EmbeddedIdentityStore;
import org.glassfish.soteria.identitystores.LDapIdentityStore;
import org.glassfish.soteria.mechanisms.BasicAuthenticationMechanism;

public class CdiExtension implements Extension {

    // Note: for now use the highlander rule: "there can be only one" for
    // both identity stores and (http) authentication mechanisms.
    // This could be extended later to support multiple
    private Bean<IdentityStore> identityStoreBean;
    private Bean<HttpAuthenticationMechanism> authenticationMechanismBean;
    private boolean httpAuthenticationMechanismFound;
    
    public void register(@Observes BeforeBeanDiscovery beforeBean, BeanManager beanManager) {
        addAnnotatedTypes(beforeBean, beanManager, 
            AutoApplySessionInterceptor.class,
            RememberMeInterceptor.class,
            LoginToContinueInterceptor.class
        );
    }

    public <T> void processBean(@Observes ProcessBean<T> eventIn, BeanManager beanManager) {

        ProcessBean<T> event = eventIn; // JDK8 u60 workaround

        // TODO: 
        // * What if multiple definitions present?
        // *   -> Make created Bean<T>s alternatives
        // *   -> Throw exception?
        
        Optional<EmbeddedIdentityStoreDefinition> optionalEmbeddedStore = getAnnotation(beanManager, event.getAnnotated(), EmbeddedIdentityStoreDefinition.class);
        if (optionalEmbeddedStore.isPresent()) {
            identityStoreBean = new CdiProducer<IdentityStore>()
                .scope(ApplicationScoped.class)
                .types(IdentityStore.class)
                .addToId(EmbeddedIdentityStoreDefinition.class)
                .create(e -> new EmbeddedIdentityStore(optionalEmbeddedStore.get().value()));
        }
        
        Optional<DataBaseIdentityStoreDefinition> optionalDBStore = getAnnotation(beanManager, event.getAnnotated(), DataBaseIdentityStoreDefinition.class);
        if (optionalDBStore.isPresent()) {
            identityStoreBean = new CdiProducer<IdentityStore>()
                .scope(ApplicationScoped.class)
                .types(IdentityStore.class)
                .addToId(DataBaseIdentityStoreDefinition.class)
                .create(e -> new DataBaseIdentityStore(optionalDBStore.get()));
        }
        
        Optional<LdapIdentityStoreDefinition> optionalLdapStore = getAnnotation(beanManager, event.getAnnotated(), LdapIdentityStoreDefinition.class);
        if (optionalLdapStore.isPresent()) {
            identityStoreBean = new CdiProducer<IdentityStore>()
                .scope(ApplicationScoped.class)
                .types(IdentityStore.class)
                .addToId(LdapIdentityStoreDefinition.class)
                .create(e -> new LDapIdentityStore(optionalLdapStore.get()));
        }
        
        Optional<BasicAuthenticationMechanismDefinition> optionalBasicMechanism = getAnnotation(beanManager, event.getAnnotated(), BasicAuthenticationMechanismDefinition.class);
        if (optionalBasicMechanism.isPresent()) {
            authenticationMechanismBean = new CdiProducer<HttpAuthenticationMechanism>()
                .scope(ApplicationScoped.class)
                .types(HttpAuthenticationMechanism.class)
                .addToId(BasicAuthenticationMechanismDefinition.class)
                .create(e -> new BasicAuthenticationMechanism(optionalBasicMechanism.get().realmName()));
        }
        
        if (event.getBean().getTypes().contains(HttpAuthenticationMechanism.class)) {
            // enabled bean implementing the HttpAuthenticationMechanism found
            httpAuthenticationMechanismFound = true;
        }
        
    }

    public void afterBean(final @Observes AfterBeanDiscovery afterBeanDiscovery) {
        if (identityStoreBean != null) {
            afterBeanDiscovery.addBean(identityStoreBean);
        }
        
        if (authenticationMechanismBean != null) {
            afterBeanDiscovery.addBean(authenticationMechanismBean);
        }
    }
    
    public boolean isHttpAuthenticationMechanismFound() {
        return httpAuthenticationMechanismFound;
    }

}
