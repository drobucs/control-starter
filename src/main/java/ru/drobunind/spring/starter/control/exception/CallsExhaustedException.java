package ru.drobunind.spring.starter.control.exception;

public class CallsExhaustedException extends RuntimeException {
	public CallsExhaustedException(String msg) {
		super(msg);
	}

	public CallsExhaustedException(Throwable cause) {
		super(cause);
	}
}