package ru.drobunind.spring.starter.cases.generic.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DogService {
	public String dog(AtomicInteger counter) {
		counter.incrementAndGet();
		return "dog";
	}
}
