/**
 * 
 */
package com.gotako.gofast.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for doing binding on field.<br>
 * The syntax is : public @BindingField(R.id.field1) String field1;
 * @author Nam
 *
 */
@Target(value= ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface BindingField {
	int id();
	boolean twoWay() default false;
}
