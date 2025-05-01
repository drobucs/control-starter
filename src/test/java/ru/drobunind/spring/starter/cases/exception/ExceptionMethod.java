package ru.drobunind.spring.starter.cases.exception;


import java.util.concurrent.atomic.AtomicInteger;

public interface ExceptionMethod {
	int AMOUNT = 10;
	int CALLS = 10;

	void method(AtomicInteger counter);
}
