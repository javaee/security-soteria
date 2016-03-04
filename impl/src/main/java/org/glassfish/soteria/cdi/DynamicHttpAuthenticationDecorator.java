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