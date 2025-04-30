package ru.drobunind.spring.starter.cases.generic.wrapper;

import org.springframework.stereotype.Component;
import ru.drobunind.spring.starter.cases.generic.AbstractServiceWrapper;
import ru.drobunind.spring.starter.cases.generic.service.FishService;
import ru.drobunind.spring.starter.control.ControlStrategy;
import ru.drobunind.spring.starter.control.annotation.Control;
import ru.drobunind.spring.starter.control.annotation.Strategy;

import static ru.drobunind.spring.starter.cases.generic.ServiceWrapper.AMOUNT;
import static ru.drobunind.spring.starter.cases.generic.ServiceWrapper.CALLS;

@Control(value = CALLS, amount = AMOUNT, strategy = @Strategy(ControlStrategy.EXCEPTION))
@Component
public class FishServiceWrapper extends AbstractServiceWrapper<FishService> {
	public FishServiceWrapper(FishService service) {
		super(service);
	}
}
