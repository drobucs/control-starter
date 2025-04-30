package ru.drobunind.spring.starter.cases.method;


import org.springframework.stereotype.Component;
import ru.drobunind.spring.starter.control.ControlStrategy;
import ru.drobunind.spring.starter.control.annotation.Control;
import ru.drobunind.spring.starter.control.annotation.Strategy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MethodImpl implements Method {

	@Control(value = CALLS,
			amount = AMOUNT,
			timeUnit = TimeUnit.SECONDS,
			strategy = @Strategy(value = ControlStrategy.EXCEPTION, amount = 100, timeUnit = TimeUnit.MILLISECONDS)
	)
	@Override
	public void controlMethod(AtomicInteger counter) {
		counter.incrementAndGet();
	}

	@Override
	public void nonControlMethod(AtomicInteger counter) {
		counter.incrementAndGet();
	}
}
