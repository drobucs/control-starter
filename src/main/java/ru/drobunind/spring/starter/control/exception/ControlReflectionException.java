package ru.drobunind.spring.starter.control.exception;


public class ControlReflectionException extends RuntimeException {
	public ControlReflectionException(String message) {
		super(message);
	}

	public ControlReflectionException(Throwable cause) {
		super(cause);
	}
}
