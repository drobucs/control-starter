package ru.drobunind.spring.starter.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.drobunind.spring.starter.control.exception.CallsExhaustedException;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class TaskRunner extends AbstractRunner implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(TaskRunner.class);
	private final int threads;
	private final AtomicLong millis;
	private final BlockingQueue<Throwable> errors = new ArrayBlockingQueue<>(1000);

	public TaskRunner(int threads, long millis) {
		super(
				Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory())
		);
		this.threads = threads;
		this.millis = new AtomicLong(millis);
	}

	public void run(List<Runnable> runnables) {
		AtomicLong start = new AtomicLong(System.currentTimeMillis());
		Runnable runnable = () -> {
			Random random = new Random();
			while (System.currentTimeMillis() - start.get() < millis.get() && !Thread.currentThread().isInterrupted()) {
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
					errors.add(e);
					log.error(e.getMessage(), e);
				}
			});
		}
	}

	public List<Throwable> getErrors() {
		return errors.stream().toList();
	}
}
