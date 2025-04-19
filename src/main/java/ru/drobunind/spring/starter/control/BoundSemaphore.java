package ru.drobunind.spring.starter.control;


import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class BoundSemaphore extends AbstractQueuedSynchronizer {
	private final int bound;

	public BoundSemaphore(int bound) {
		if (bound <= 0)
			throw new IllegalArgumentException("Bound must be positive");
		this.bound = bound;
		setState(bound);
	}

	public void acquire() {
		acquireShared(1);
	}

	public void maximize() {
		releaseShared(bound);
	}


	@Override
	protected int tryAcquireShared(int acquires) {
		while (true) {
			int state = getState();
			int remaining = state - acquires;
			if (remaining < 0 || compareAndSetState(state, remaining)) {
				return remaining;
			}
		}
	}

	@Override
	protected boolean tryReleaseShared(int releases) {
		while (true) {
			int state = getState();
			int nextState = state + releases;
			if (nextState > bound)
				nextState = bound;
			if (compareAndSetState(state, nextState)) {
				return true;
			}
		}
	}
}
