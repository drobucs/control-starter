package ru.drobunind.spring.starter.cases.generic;


import java.util.concurrent.atomic.AtomicInteger;

public interface AnimalClient {
	int AMOUNT = 10;
	int CALLS = 200;

	String cat(AtomicInteger counter);

	String dog(AtomicInteger counter);

	String bird(AtomicInteger counter);

	String fish(AtomicInteger counter);
}
