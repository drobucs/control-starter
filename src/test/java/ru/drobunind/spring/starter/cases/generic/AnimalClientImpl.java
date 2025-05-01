package ru.drobunind.spring.starter.cases.generic;


import org.springframework.stereotype.Component;
import ru.drobunind.spring.starter.cases.generic.service.BirdService;
import ru.drobunind.spring.starter.cases.generic.service.CatService;
import ru.drobunind.spring.starter.cases.generic.service.DogService;
import ru.drobunind.spring.starter.cases.generic.service.FishService;
import ru.drobunind.spring.starter.control.ControlStrategy;
import ru.drobunind.spring.starter.control.annotation.Control;
import ru.drobunind.spring.starter.control.annotation.Strategy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.drobunind.spring.starter.cases.generic.AnimalClient.AMOUNT;
import static ru.drobunind.spring.starter.cases.generic.AnimalClient.CALLS;

@Control(value = CALLS,
		amount = AMOUNT,
		timeUnit = TimeUnit.SECONDS,
		strategy = @Strategy(ControlStrategy.EXCEPTION_TIMEOUT)
)
@Component
public class AnimalClientImpl implements AnimalClient {
	private final ServiceWrapper<CatService> catServiceWrapper;
	private final ServiceWrapper<DogService> dogServiceWrapper;
	private final ServiceWrapper<BirdService> birdServiceWrapper;
	private final ServiceWrapper<FishService> fishServiceWrapper;

	public AnimalClientImpl(ServiceWrapper<CatService> catServiceWrapper,
	                        ServiceWrapper<DogService> dogServiceWrapper,
	                        ServiceWrapper<BirdService> birdServiceWrapper,
	                        ServiceWrapper<FishService> fishServiceWrapper) {
		this.catServiceWrapper = catServiceWrapper;
		this.dogServiceWrapper = dogServiceWrapper;
		this.birdServiceWrapper = birdServiceWrapper;
		this.fishServiceWrapper = fishServiceWrapper;
	}

	@Override
	public String cat(AtomicInteger counter) {
		return catServiceWrapper.exec(s -> s.cat(counter));
	}

	@Override
	public String dog(AtomicInteger counter) {
		return dogServiceWrapper.exec(s -> s.dog(counter));
	}

	@Override
	public String bird(AtomicInteger counter) {
		return birdServiceWrapper.exec(s -> s.bird(counter));

	}

	@Override
	public String fish(AtomicInteger counter) {
		return fishServiceWrapper.exec(s -> s.fish(counter));
	}
}
