package ru.drobunind.spring.starter.cases.generic;


import java.util.function.Function;

public abstract class AbstractServiceWrapper<S> implements ServiceWrapper<S> {
	private final S service;

	public AbstractServiceWrapper(S service) {
		this.service = service;
	}

	@Override
	public <R> R exec(Function<S, R> call) {
		return call.apply(service);
	}
}
