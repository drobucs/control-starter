package ru.drobunind.spring.starter.cases.exception;


import org.springframework.stereotype.Component;
import ru.drobunind.spring.starter.control.ControlStrategy;
import ru.drobunind.spring.starter.control.annotation.Control;
import ru.drobunind.spring.starter.control.annotation.Strategy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.drobunind.spring.starter.cases.exception.ExceptionMethodImpl.AMOUNT;
import static ru.drobunind.spring.starter.cases.exception.ExceptionMethodImpl.CALLS;

@Control(value = CALLS,
		amount = AMOUNT,
		timeUnit = TimeUnit.SECONDS,
		strategy = @Strategy(ControlStrategy.EXCEPTION_TIMEOUT)
)
@Component
public class ExceptionMethodImpl implements ExceptionMethod {
	@Override
	public void method(AtomicInteger counter) {
		counter.incrementAndGet();
	}
}
