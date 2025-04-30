package ru.drobunind.spring.starter.control;


import ru.drobunind.spring.starter.control.exception.CallsExhaustedException;

public enum ControlStrategy {
	/**
	 * Blocks thread until the method allows itself to be called
	 */
	BLOCKING,
	/**
	 * Throws inheritor of {@link RuntimeException} if the method failed to be called within the timeout.
	 * By default, it is {@link CallsExhaustedException}
	 */
	EXCEPTION_TIMEOUT,
	/**
	 * Throws inheritor of {@link RuntimeException} if the method failed to be called.
	 * By default, it is {@link CallsExhaustedException}
	 */
	EXCEPTION
}
