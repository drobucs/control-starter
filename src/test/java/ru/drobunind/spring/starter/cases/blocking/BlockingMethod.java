package ru.drobunind.spring.starter.cases.blocking;


import java.util.concurrent.atomic.AtomicInteger;

public interface BlockingMethod {
	int AMOUNT = 5;
	int CALLS = 10;

	void method(AtomicInteger counter);
}
