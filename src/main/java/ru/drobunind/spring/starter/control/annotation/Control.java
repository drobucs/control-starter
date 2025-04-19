package ru.drobunind.spring.starter.control.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Control {
	/**
	 * The number of times the method will be run in a given time interval
	 */
	int value() default 1;

	/**
	 * Time interval
	 */
	long fixedRate() default 1;

	/**
	 * Units of an interval
	 */
	TimeUnit timeUnit() default TimeUnit.SECONDS;
}
