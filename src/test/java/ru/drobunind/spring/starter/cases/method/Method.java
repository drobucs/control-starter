package ru.drobunind.spring.starter.cases.method;


import java.util.concurrent.atomic.AtomicInteger;

public interface Method {
	int AMOUNT = 20;
	int CALLS = 200;

	void controlMethod(AtomicInteger counter);

	void nonControlMethod(AtomicInteger counter);
}
