package ru.drobunind.spring.starter.control;

import jakarta.annotation.PreDestroy;
import ru.drobunind.spring.starter.control.ControlAnnotationBeanPostProcessor.AnnotationInfo;

import java.util.Map;
import java.util.concurrent.*;

public class ControlAnnotationHandler {

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
						info.annotation().fixedRate(),
						info.annotation().timeUnit()
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

		public ControlState(ScheduledExecutorService executor, int maxCalls, long fixedRate, TimeUnit timeUnit) {
			this.semaphore = new BoundSemaphore(maxCalls);
			this.executor = executor;
			this.executor.scheduleAtFixedRate(semaphore::maximize, fixedRate, fixedRate, timeUnit);
		}

		public Object invoke(SupplierThrowable runnable) throws Throwable {
			semaphore.acquire();
			return runnable.get();
		}

		@Override
		public void close() {
			executor.close();
		}
	}
}