package org.glassfish.soteria.cdi;

import javax.enterprise.inject.Default;
import javax.enterprise.util.AnnotationLiteral;

/**
 * An annotation literal for @Default.
 * 
 */
@SuppressWarnings("all")
class DefaultAnnotationLiteral extends AnnotationLiteral<Default> implements Default {
    private static final long serialVersionUID = 1L;
}
