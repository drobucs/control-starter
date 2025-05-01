package ru.drobunind.spring.starter.cases.clazz;


import java.util.concurrent.atomic.AtomicInteger;

public interface ThreeMethods extends TwoMethods {
	int AMOUNT = 10;
	int CALLS = 10;

	void method3(AtomicInteger counter);
}
