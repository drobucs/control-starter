package ru.drobunind.spring.starter.service;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.drobunind.spring.starter.cases.blocking.BlockingMethod;
import ru.drobunind.spring.starter.cases.blocking.BlockingMethodImpl;
import ru.drobunind.spring.starter.cases.clazz.ThreeMethods;
import ru.drobunind.spring.starter.cases.exception.ExceptionMethod;
import ru.drobunind.spring.starter.cases.exception.ExceptionMethodImpl;
import ru.drobunind.spring.starter.cases.exclude.ExcludeMethod;
import ru.drobunind.spring.starter.cases.method.Method;
import ru.drobunind.spring.starter.control.exception.CallsExhaustedException;
import ru.drobunind.spring.starter.utils.TaskRunner;
import ru.drobunind.spring.starter.utils.TaskRunnerLimit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
class ControlAnnotationTest {
	private static final Logger log = LoggerFactory.getLogger(ControlAnnotationTest.class);
	@Autowired
	ThreeMethods threeMethods;

	@Autowired
	ExceptionMethod exceptionMethod;

	@Autowired
	BlockingMethod blockingMethod;

	@Autowired
	ExcludeMethod excludeMethod;

	@Autowired
	Method method;

	@Test
	void testClass() throws InterruptedException {
		AtomicInteger counter = new AtomicInteger();
		List<Runnable> runnables = List.of(
				() -> threeMethods.method1(counter),
				() -> threeMethods.method2(counter),
				() -> threeMethods.method3(counter)
		);
		var millis = Duration.of(ThreeMethods.AMOUNT / 2, ChronoUnit.SECONDS).toMillis();
		try (TaskRunner taskRunner = new TaskRunner(10, millis)) {
			taskRunner.run(runnables);
			Thread.sleep(millis);
		}
		assertEquals(ThreeMethods.CALLS, counter.get());
	}

	@Test
	void testThrowsException() throws InterruptedException {
		AtomicInteger counter = new AtomicInteger();
		List<Runnable> runnables = List.of(
				() -> exceptionMethod.method(counter)
		);
		var millis = Duration.of(ExceptionMethodImpl.AMOUNT / 2, ChronoUnit.SECONDS).toMillis();
		try (TaskRunner taskRunner = new TaskRunner(10, millis)) {
			taskRunner.run(runnables);
			Thread.sleep(millis);
		}
		assertEquals(ExceptionMethodImpl.CALLS, counter.get());
		assertThrows(CallsExhaustedException.class, () -> exceptionMethod.method(counter));
	}

	@Test
	void testBlocking() {
		AtomicInteger counter = new AtomicInteger();
		List<Runnable> runnables = List.of(() -> blockingMethod.method(counter));
		int n = 2;
		int limit = n * BlockingMethodImpl.CALLS + 1;
		try (TaskRunnerLimit taskRunner = new TaskRunnerLimit(10)) {
			taskRunner.run(runnables, limit);
			await().atLeast(n * BlockingMethodImpl.AMOUNT, TimeUnit.SECONDS)
					.and()
					.atMost((n + 1) * BlockingMethodImpl.AMOUNT, TimeUnit.SECONDS)
					.until(
							() -> counter.get() == limit
					);
		}
	}


	private void testWithoutControl(List<Consumer<AtomicInteger>> controlMethods,
	                                List<Consumer<AtomicInteger>> nonControlMethods,
	                                int amount,
	                                int calls) {
		AtomicInteger counter = new AtomicInteger();
		AtomicInteger excludedCounter = new AtomicInteger();
		List<Runnable> runnables = Stream.concat(
						controlMethods.stream().map(c -> (Runnable) () -> c.accept(counter)),
						nonControlMethods.stream().map(c -> () -> c.accept(excludedCounter))
				)
				.toList();
		long millis = Duration.of(amount, TimeUnit.SECONDS.toChronoUnit()).toMillis();
		try (TaskRunner taskRunner = new TaskRunner(10, millis)) {
			taskRunner.run(runnables);
			await().atMost(amount / 2, TimeUnit.SECONDS)
					.pollInterval(100, TimeUnit.MILLISECONDS)
					.until(
							() -> {
								log.info("ex: {}, c: {}", excludedCounter.get(), counter.get());
								return excludedCounter.get() > calls
										&& counter.get() <= calls;
							}
					);
		}
	}


	@Test
	void testExclude() {
		testWithoutControl(
				List.of(excludeMethod::method),
				List.of(excludeMethod::excludedMethod),
				ExcludeMethod.AMOUNT,
				ExcludeMethod.CALLS
		);
	}

	@Test
	void testMethod() {
		testWithoutControl(
				List.of(method::controlMethod),
				List.of(method::nonControlMethod),
				Method.AMOUNT,
				Method.CALLS
		);
	}
}
