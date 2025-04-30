package ru.drobunind.spring.starter.cases.generic.wrapper;

import org.springframework.stereotype.Component;
import ru.drobunind.spring.starter.cases.generic.AbstractServiceWrapper;
import ru.drobunind.spring.starter.cases.generic.service.BirdService;

@Component
public class BirdServiceWrapper extends AbstractServiceWrapper<BirdService> {
	public BirdServiceWrapper(BirdService service) {
		super(service);
	}
}
