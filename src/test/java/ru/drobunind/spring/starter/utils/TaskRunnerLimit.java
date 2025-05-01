package ru.drobunind.spring.starter.utils;


import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskRunnerLimit extends AbstractRunner {
	private final int threads;

	public TaskRunnerLimit(int threads) {
		super(
				Executors.newFixedThreadPool(threads, Thread.ofVirtual().factory())
		);
		this.threads = threads;
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
}
