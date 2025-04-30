package ru.drobunind.spring.starter.cases.clazz;

import org.springframework.stereotype.Component;
import ru.drobunind.spring.starter.control.ControlStrategy;
import ru.drobunind.spring.starter.control.annotation.Control;
import ru.drobunind.spring.starter.control.annotation.Strategy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.drobunind.spring.starter.cases.clazz.ThreeMethodsImpl.AMOUNT;
import static ru.drobunind.spring.starter.cases.clazz.ThreeMethodsImpl.CALLS;

@Control(value = CALLS,
		amount = AMOUNT,
		timeUnit = TimeUnit.SECONDS,
		strategy = @Strategy(ControlStrategy.EXCEPTION_TIMEOUT)
)
@Component
public class ThreeMethodsImpl implements ThreeMethods {
	@Override
	public void method1(AtomicInteger counter) {
		counter.incrementAndGet();
	}

	@Override
	public void method2(AtomicInteger counter) {
		counter.incrementAndGet();
	}

	@Override
	public void method3(AtomicInteger counter) {
		counter.incrementAndGet();
	}
}
