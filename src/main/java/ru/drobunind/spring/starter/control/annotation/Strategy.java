package ru.drobunind.spring.starter.control.annotation;

import ru.drobunind.spring.starter.control.ControlStrategy;
import ru.drobunind.spring.starter.control.exception.CallsExhaustedException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Strategy {
	ControlStrategy value() default ControlStrategy.BLOCKING;

	int amount() default 1;

	TimeUnit timeUnit() default TimeUnit.SECONDS;

	String message() default "Method calls exhausted";

	Class<? extends RuntimeException> exception() default CallsExhaustedException.class;
}
