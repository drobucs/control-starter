package ru.drobunind.spring.starter.utils;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class AbstractRunner implements AutoCloseable {
	public final ExecutorService executor;

	public AbstractRunner(ExecutorService executor) {
		this.executor = executor;
	}

	public void awaitTermination() throws InterruptedException {
		if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
			throw new RuntimeException("Await termination too long");
		}
	}

	@Override
	public void close() throws InterruptedException {
		executor.shutdownNow();
		awaitTermination();
	}
}
