package ru.drobunind.spring.starter.cases.generic.wrapper;

import org.springframework.stereotype.Component;
import ru.drobunind.spring.starter.cases.generic.AbstractServiceWrapper;
import ru.drobunind.spring.starter.cases.generic.service.DogService;

@Component
public class DogServiceWrapper extends AbstractServiceWrapper<DogService> {
	public DogServiceWrapper(DogService service) {
		super(service);
	}
}
