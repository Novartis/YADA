/**
 * 
 */
package com.novartis.opensource.yada.plugin;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author dvaron
 * @since 8.7.6
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target(TYPE)

@interface SecurityPreprocessor {

}
