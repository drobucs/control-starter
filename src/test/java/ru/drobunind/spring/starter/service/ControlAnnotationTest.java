package ru.drobunind.spring.starter.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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
import ru.drobunind.spring.starter.cases.generic.AnimalClient;
import ru.drobunind.spring.starter.cases.generic.ServiceWrapper;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
class ControlAnnotationTest {
	private static final Logger log = LoggerFactory.getLogger(ControlAnnotationTest.class);
	static final int THREADS = 3;

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

	@Autowired
	AnimalClient animalClient;

	@Autowired
	List<ServiceWrapper<?>> serviceWrappers;

	@Test
	void testClass() {
		AtomicInteger counter = new AtomicInteger();
		List<Runnable> runnables = List.of(
				() -> threeMethods.method1(counter),
				() -> threeMethods.method2(counter),
				() -> threeMethods.method3(counter)
		);
		var duration = Duration.of(ThreeMethods.AMOUNT + 3, ChronoUnit.SECONDS);
		try (TaskRunner taskRunner = new TaskRunner(THREADS, duration.toMillis())) {
			taskRunner.run(runnables);
			await().atLeast(ThreeMethods.AMOUNT, TimeUnit.SECONDS)
					.atMost(2 * ThreeMethods.AMOUNT - 2, TimeUnit.SECONDS)
					.pollInterval(1, TimeUnit.SECONDS)
					.until(
							() -> {
								log.info("counter: {}", counter.get());
								return counter.get() > ThreeMethods.CALLS;
							}
					);
			assertThat(taskRunner.getErrors().size()).isEqualTo(0);
			assertThat(counter.get()).isGreaterThan(ThreeMethods.CALLS);
		}
	}

	@Test
	void testThrowsException() {
		AtomicInteger counter = new AtomicInteger();
		List<Runnable> runnables = List.of(
				() -> exceptionMethod.method(counter)
		);
		var duration = Duration.of(ExceptionMethodImpl.AMOUNT, ChronoUnit.SECONDS);
		try (TaskRunner taskRunner = new TaskRunner(THREADS, duration.toMillis())) {
			taskRunner.run(runnables);
			await().atMost(duration.minusSeconds(ExceptionMethodImpl.AMOUNT / 2))
					.pollInterval(1, TimeUnit.SECONDS)
					.untilAsserted(
							() -> {
								log.info("counter: {}", counter.get());
								assertThrows(CallsExhaustedException.class, () -> exceptionMethod.method(counter));
							}
					);
			assertThat(taskRunner.getErrors().size()).isEqualTo(0);
			assertEquals(ExceptionMethodImpl.CALLS, counter.get());
		}
	}

	@Test
	void testBlocking() {
		AtomicInteger counter = new AtomicInteger();
		List<Runnable> runnables = List.of(() -> blockingMethod.method(counter));
		int n = 2;
		int limit = n * BlockingMethodImpl.CALLS + 1;
		try (TaskRunnerLimit taskRunner = new TaskRunnerLimit(THREADS)) {
			taskRunner.run(runnables, limit);
			await().atLeast(n * BlockingMethodImpl.AMOUNT, TimeUnit.SECONDS)
					.and()
					.atMost((n + 1) * BlockingMethodImpl.AMOUNT, TimeUnit.SECONDS)
					.pollInterval(1, TimeUnit.SECONDS)
					.until(
							() -> {
								log.info("counter: {}, limit: {}", counter.get(), limit);
								return counter.get() == limit;
							}
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
		try (TaskRunner taskRunner = new TaskRunner(THREADS, millis)) {
			taskRunner.run(runnables);
			await().atMost(amount / 2, TimeUnit.SECONDS)
					.pollInterval(100, TimeUnit.MILLISECONDS)
					.until(
							() -> {
								log.info("excludedCounter: {}, counter: {}", excludedCounter.get(), counter.get());
								return excludedCounter.get() > calls
										&& counter.get() <= calls;
							}
					);
			assertThat(taskRunner.getErrors().size()).isEqualTo(0);
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

	@Test
	void testGeneric() {
		AtomicInteger counter = new AtomicInteger();
		List<Runnable> runnables = List.of(
				() -> animalClient.cat(counter),
				() -> animalClient.dog(counter),
				() -> animalClient.fish(counter),
				() -> animalClient.bird(counter)
		);
		var duration = Duration.of(ServiceWrapper.AMOUNT + 5, ChronoUnit.SECONDS);
		try (TaskRunner taskRunner = new TaskRunner(THREADS, duration.toMillis())) {
			taskRunner.run(runnables);
			await().atLeast(AnimalClient.AMOUNT, TimeUnit.SECONDS)
					.atMost(2 * AnimalClient.AMOUNT - 2, TimeUnit.SECONDS)
					.pollInterval(1, TimeUnit.SECONDS)
					.until(
							() -> {
								log.info("counter: {}", counter.get());
								return counter.get() > ServiceWrapper.CALLS * serviceWrappers.size();
							}
					);
			assertThat(taskRunner.getErrors().size()).isEqualTo(0);
			assertThat(counter.get()).isLessThanOrEqualTo(AnimalClient.CALLS);
		}

	}

	void logHead(String text) {
		log.info("\n\n====================== {} ======================\n\n", text);
	}

	@BeforeEach
	void beforeEach(TestInfo testInfo) {
		logHead("Starting " + testInfo.getDisplayName());
	}

	@AfterEach
	void afterEach(TestInfo testInfo) {
		logHead("Finished " + testInfo.getDisplayName());
	}
}
