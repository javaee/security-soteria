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
