package ru.drobunind.spring.starter.cases.generic.wrapper;

import org.springframework.stereotype.Component;
import ru.drobunind.spring.starter.cases.generic.AbstractServiceWrapper;
import ru.drobunind.spring.starter.cases.generic.service.FishService;

@Component
public class FishServiceWrapper extends AbstractServiceWrapper<FishService> {
	public FishServiceWrapper(FishService service) {
		super(service);
	}
}
