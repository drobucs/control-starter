package ru.drobunind.spring.starter.cases.generic;


import java.util.function.Function;

public interface ServiceWrapper<S> {
	<R> R exec(Function<S, R> call);
}
