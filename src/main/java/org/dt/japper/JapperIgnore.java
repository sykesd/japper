package org.dt.japper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to tell the mapper to ignore this property.
 * <p>
 *     This should be placed on either the getter or the setter method of the property.
 *     Placing it on either one will tell Japper to ignore this property during matching.
 * </p>
 * <p>
 *     The initial use-case for this is to allow for the prevention of infinite loops.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JapperIgnore {
}
