package ru.drobunind.spring.starter.cases.generic.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BirdService {
	public String bird(AtomicInteger counter) {
		counter.incrementAndGet();
		return "bird";
	}
}
