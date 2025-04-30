package ru.drobunind.spring.starter.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.drobunind.spring.starter.control.exception.CallsExhaustedException;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class TaskRunner implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(TaskRunner.class);
	private final int threads;
	private final AtomicLong millis;
	private final ExecutorService executor;
	private final List<Throwable> errors = new ArrayList<>();

	public TaskRunner(int threads, long millis) {
		this.threads = threads;
		this.millis = new AtomicLong(millis);
		this.executor = Executors.newFixedThreadPool(threads, Thread.ofVirtual().factory());
	}

	public void run(List<Runnable> runnables) {
		AtomicLong start = new AtomicLong(System.currentTimeMillis());
		Runnable runnable = () -> {
			Random random = new Random();
			while (System.currentTimeMillis() - start.get() < millis.get()) {
				int randInt = random.nextInt(runnables.size());
				try {
					runnables.get(randInt).run();
				} catch (CallsExhaustedException ignore) {
				}
			}
		};

		for (int i = 0; i < threads; ++i) {
			executor.submit(() -> {
				try {
					runnable.run();
				} catch (UndeclaredThrowableException e) {
					if (!(e.getCause() instanceof InterruptedException)) {
						throw e;
					}
				} catch (Exception e) {
					synchronized (errors) {
						errors.add(e);
					}
					log.error(e.getMessage(), e);
				}
			});
		}
	}

	public List<Throwable> getErrors() {
		return errors;
	}

	@Override
	public void close() {
		executor.shutdownNow();
	}
}
