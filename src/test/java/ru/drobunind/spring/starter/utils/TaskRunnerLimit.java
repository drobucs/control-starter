package ru.drobunind.spring.starter.utils;


import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskRunnerLimit implements AutoCloseable {
	private final int threads;
	private final ExecutorService executor;

	public TaskRunnerLimit(int threads) {
		this.threads = threads;
		this.executor = Executors.newFixedThreadPool(threads, Thread.ofVirtual().factory());
	}

	public void run(List<Runnable> runnables, int limit) {
		AtomicInteger counter = new AtomicInteger();
		Runnable runnable = () -> {
			Random random = new Random();
			while (counter.incrementAndGet() <= limit) {
				runnables.get(random.nextInt(runnables.size())).run();
			}
		};
		for (int i = 0; i < threads; ++i) {
			executor.submit(runnable);
		}
	}

	@Override
	public void close() {
		executor.shutdownNow();
	}
}
