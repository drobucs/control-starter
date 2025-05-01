package ru.drobunind.spring.starter.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ControlAnnotationTest extends BaseTest {
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

	@Autowired
	AnimalClient animalClient;

	@Autowired
	List<ServiceWrapper<?>> serviceWrappers;

	@RepeatedTest(5)
	void testClass() {
		AtomicInteger counter = new AtomicInteger();
		List<Runnable> runnables = List.of(
				() -> threeMethods.method1(counter),
				() -> threeMethods.method2(counter),
				() -> threeMethods.method3(counter)
		);
		var duration = Duration.of(2 * ThreeMethods.AMOUNT, ChronoUnit.SECONDS);
		withTaskRunner(duration.toMillis(), runnables, taskRunner -> {
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
		});
	}

	@RepeatedTest(5)
	void testThrowsException() {
		AtomicInteger counter = new AtomicInteger();
		List<Runnable> runnables = List.of(
				() -> exceptionMethod.method(counter)
		);
		var duration = Duration.of(2 * ExceptionMethodImpl.AMOUNT, ChronoUnit.SECONDS);
		withTaskRunner(duration.toMillis(), runnables, taskRunner -> {
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
		});
	}

	@RepeatedTest(5)
	void testBlocking() {
		AtomicInteger counter = new AtomicInteger();
		List<Runnable> runnables = List.of(() -> blockingMethod.method(counter));
		int n = 2;
		int limit = n * BlockingMethodImpl.CALLS + 1;
		withTaskRunnerLimit(limit, runnables, taskRunner -> {
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
		});
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
		withTaskRunner(millis, runnables, taskRunner -> {
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
		});
	}


	@RepeatedTest(5)
	void testExclude() {
		testWithoutControl(
				List.of(excludeMethod::method),
				List.of(excludeMethod::excludedMethod),
				ExcludeMethod.AMOUNT,
				ExcludeMethod.CALLS
		);
	}

	@RepeatedTest(5)
	void testMethod() {
		testWithoutControl(
				List.of(method::controlMethod),
				List.of(method::nonControlMethod),
				Method.AMOUNT,
				Method.CALLS
		);
	}

	@RepeatedTest(5)
	void testGeneric() {
		AtomicInteger counter = new AtomicInteger();
		List<Runnable> runnables = List.of(
				() -> animalClient.cat(counter),
				() -> animalClient.dog(counter),
				() -> animalClient.fish(counter),
				() -> animalClient.bird(counter)
		);
		var duration = Duration.of(2 * AnimalClient.AMOUNT, ChronoUnit.SECONDS);
		withTaskRunner(duration.toMillis(), runnables, taskRunner -> {
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
		});
	}

	@BeforeEach
	void beforeTest(TestInfo testInfo) {
		logHead(getMethodName(testInfo) + ": " + testInfo.getDisplayName());
	}

	@AfterEach
	void afterEach(TestInfo testInfo) {
		logHead("Finished " + getMethodName(testInfo) + ":" + testInfo.getDisplayName());
	}

	String getMethodName(TestInfo testInfo) {
		return testInfo.getTestMethod().map(java.lang.reflect.Method::getName).orElse("[unknown test]");
	}

	@Override
	Logger getLogger() {
		return log;
	}
}
