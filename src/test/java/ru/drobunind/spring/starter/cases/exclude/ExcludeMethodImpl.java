package ru.drobunind.spring.starter.cases.exclude;

import org.springframework.stereotype.Component;
import ru.drobunind.spring.starter.control.ControlStrategy;
import ru.drobunind.spring.starter.control.annotation.Control;
import ru.drobunind.spring.starter.control.annotation.ControlExclude;
import ru.drobunind.spring.starter.control.annotation.Strategy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.drobunind.spring.starter.cases.exclude.ExcludeMethod.AMOUNT;
import static ru.drobunind.spring.starter.cases.exclude.ExcludeMethod.CALLS;

@Control(value = CALLS,
		amount = AMOUNT,
		timeUnit = TimeUnit.SECONDS,
		strategy = @Strategy(value = ControlStrategy.EXCEPTION, amount = 100, timeUnit = TimeUnit.MILLISECONDS)
)
@Component
public class ExcludeMethodImpl implements ExcludeMethod {
	@Override
	public void method(AtomicInteger counter) {
		counter.incrementAndGet();
	}

	@ControlExclude
	@Override
	public void excludedMethod(AtomicInteger counter) {
		counter.incrementAndGet();
	}
}
