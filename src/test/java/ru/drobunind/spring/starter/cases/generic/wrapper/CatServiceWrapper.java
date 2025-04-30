package ru.drobunind.spring.starter.cases.generic.wrapper;

import org.springframework.stereotype.Component;
import ru.drobunind.spring.starter.cases.generic.AbstractServiceWrapper;
import ru.drobunind.spring.starter.cases.generic.service.CatService;

@Component
public class CatServiceWrapper extends AbstractServiceWrapper<CatService> {
	public CatServiceWrapper(CatService service) {
		super(service);
	}
}
