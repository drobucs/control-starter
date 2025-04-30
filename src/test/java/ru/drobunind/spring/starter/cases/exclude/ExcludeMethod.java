package ru.drobunind.spring.starter.cases.exclude;

import java.util.concurrent.atomic.AtomicInteger;

public interface ExcludeMethod {
	int AMOUNT = 20;
	int CALLS = 200;

	void method(AtomicInteger counter);

	void excludedMethod(AtomicInteger counter);
}
