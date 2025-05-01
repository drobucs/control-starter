package ru.drobunind.spring.starter.service;


import org.slf4j.Logger;
import ru.drobunind.spring.starter.utils.TaskRunner;
import ru.drobunind.spring.starter.utils.TaskRunnerLimit;

import java.util.List;
import java.util.function.Consumer;

public abstract class BaseTest {
	static final int THREADS = 3;

	void withTaskRunner(long millis, List<Runnable> runnables, Consumer<TaskRunner> checks) {
		try (TaskRunner taskRunner = new TaskRunner(THREADS, millis)) {
			taskRunner.run(runnables);
			checks.accept(taskRunner);
		} catch (InterruptedException e) {
			getLogger().error(e.getMessage(), e);
		}
	}

	void withTaskRunnerLimit(int limit, List<Runnable> runnables, Consumer<TaskRunnerLimit> checks) {
		try (TaskRunnerLimit taskRunner = new TaskRunnerLimit(THREADS)) {
			taskRunner.run(runnables, limit);
			checks.accept(taskRunner);
		} catch (InterruptedException e) {
			getLogger().error(e.getMessage(), e);
		}
	}

	abstract Logger getLogger();

	void logHead(String text) {
		getLogger().info("\n\n====================== {} ======================\n\n", text);
	}
}
