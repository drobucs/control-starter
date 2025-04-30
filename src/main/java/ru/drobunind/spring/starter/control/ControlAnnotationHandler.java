package ru.drobunind.spring.starter.control;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.drobunind.spring.starter.control.ControlAnnotationBeanPostProcessor.AnnotationInfo;
import ru.drobunind.spring.starter.control.annotation.Strategy;
import ru.drobunind.spring.starter.control.core.BoundSemaphore;
import ru.drobunind.spring.starter.control.exception.CallsExhaustedException;
import ru.drobunind.spring.starter.control.exception.ControlReflectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ControlAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(ControlAnnotationHandler.class);
	private final Map<String, ControlState> methodStateMap = new ConcurrentHashMap<>();
	private final ScheduledExecutorService executor;

	public ControlAnnotationHandler(ScheduledExecutorService executor) {
		this.executor = executor;
	}

	@SuppressWarnings("resource")
	public Object invoke(SupplierThrowable runnable, String controlId, AnnotationInfo info) throws Throwable {
		ControlState state = methodStateMap.computeIfAbsent(controlId, id ->
				new ControlState(
						executor,
						info.annotation().value(),
						info.annotation().amount(),
						info.annotation().timeUnit(),
						info.annotation().strategy()
				)
		);
		return state.invoke(runnable);
	}

	@PreDestroy
	public void onDestroy() {
		methodStateMap.forEach((stateId, state) -> state.close());
	}

	private static class ControlState implements AutoCloseable {
		private final BoundSemaphore semaphore;
		private final ScheduledExecutorService executor;
		private final Strategy strategy;

		public ControlState(ScheduledExecutorService executor,
		                    int maxCalls,
		                    long fixedRate,
		                    TimeUnit timeUnit,
		                    Strategy strategy) {
			this.semaphore = new BoundSemaphore(maxCalls);
			this.executor = executor;
			this.executor.scheduleAtFixedRate(semaphore::maximize, fixedRate, fixedRate, timeUnit);
			this.strategy = strategy;
		}

		public Object invoke(SupplierThrowable runnable) throws Throwable {
			switch (strategy.value()) {
				case BLOCKING -> semaphore.acquire();
				case EXCEPTION -> {
					if (!semaphore.tryAcquire()) {
						throw getException(strategy.exception(), strategy.message());
					}
				}
				case EXCEPTION_TIMEOUT -> {
					if (!semaphore.tryAcquire(strategy.amount(), strategy.timeUnit())) {
						throw getException(strategy.exception(), strategy.message());
					}
				}
			}
			return runnable.get();
		}

		private RuntimeException getException(Class<? extends RuntimeException> exception, String message) {
			if (exception == CallsExhaustedException.class) {
				return new CallsExhaustedException(message);
			}
			var constructors = exception.getConstructors();
			List<Constructor<?>> cntrs = Arrays.stream(constructors)
					.filter(c -> Modifier.isPublic(c.getModifiers()))
					.filter(c -> c.getParameterCount() <= 1)
					.toList();
			var stringConstructor = cntrs.stream()
					.filter(c -> c.getParameterCount() == 1)
					.filter(c -> c.getParameterTypes()[0] == String.class)
					.findFirst()
					.orElse(null);
			var defaultConstructor = cntrs.stream()
					.filter(c -> c.getParameterCount() == 0)
					.findFirst()
					.orElse(null);
			if (stringConstructor == null && defaultConstructor == null) {
				log.error("Cannot instantiate exception [{}] with message [{}]", exception.getCanonicalName(), message);
				return new CallsExhaustedException(message);
			}

			try {
				if (stringConstructor != null) {
					return (RuntimeException) stringConstructor.newInstance(message);
				}
				return (RuntimeException) defaultConstructor.newInstance();
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				log.error(e.getMessage(), e);
				log.error("Cannot create exception: {}", e.getMessage());
				throw new ControlReflectionException(e);
			}
		}

		@Override
		public void close() {
			executor.close();
		}
	}
}