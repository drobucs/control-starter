package ru.drobunind.spring.starter.cases.clazz;


import java.util.concurrent.atomic.AtomicInteger;

public interface ThreeMethods extends TwoMethods {
	int AMOUNT = 5;
	int CALLS = 200;

	void method3(AtomicInteger counter);
}
