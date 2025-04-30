package ru.drobunind.spring.starter.cases.generic;


import java.util.function.Function;

public interface ServiceWrapper<S> {
	int AMOUNT = 10;
	int CALLS = 10;

	<R> R exec(Function<S, R> call);
}
