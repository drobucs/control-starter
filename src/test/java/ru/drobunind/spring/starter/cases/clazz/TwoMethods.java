package ru.drobunind.spring.starter.cases.clazz;


import java.util.concurrent.atomic.AtomicInteger;

public interface TwoMethods extends OneMethod {
	void method2(AtomicInteger counter);
}
