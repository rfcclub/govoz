/**
 * 
 */
package com.gotako.gofast.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for doing binding on collection.<br>
 * The syntax is : public @BindingCollection(R.id.field1,R.layout.layout1) String field1;
 * @author Nam
 *
 */
@Target(value= ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface BindingCollection {
	int id();
	int layout();
	int groupLayout() default 0; 
	boolean twoWay() default false;
}
