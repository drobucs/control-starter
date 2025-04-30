package ru.drobunind.spring.starter.cases.blocking;


import org.springframework.stereotype.Component;
import ru.drobunind.spring.starter.control.annotation.Control;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.drobunind.spring.starter.cases.blocking.BlockingMethodImpl.AMOUNT;
import static ru.drobunind.spring.starter.cases.blocking.BlockingMethodImpl.CALLS;

@Control(value = CALLS, amount = AMOUNT, timeUnit = TimeUnit.SECONDS)
@Component
public class BlockingMethodImpl implements BlockingMethod {
	@Override
	public void method(AtomicInteger counter) {
		counter.incrementAndGet();
	}
}
