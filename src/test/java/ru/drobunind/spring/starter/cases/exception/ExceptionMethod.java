package ru.drobunind.spring.starter.cases.exception;


import java.util.concurrent.atomic.AtomicInteger;

public interface ExceptionMethod {
	int AMOUNT = 20;
	int CALLS = 10;

	void method(AtomicInteger counter);
}
