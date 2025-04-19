package ru.drobunind.spring.starter.control;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

@Configuration
public class ControlAutoConfiguration {
	public final static String BEAN_NAME_THREAD_FACTORY = "controlHandlerThreadFactory";
	public final static String BEAN_NAME_EXECUTOR_SERVICE = "controlScheduledExecutorService";

	@Bean(BEAN_NAME_THREAD_FACTORY)
	@ConditionalOnMissingBean(name = BEAN_NAME_THREAD_FACTORY)
	public ThreadFactory controlHandlerThreadFactory() {
		return Thread.ofVirtual().factory();
	}

	@Bean(BEAN_NAME_EXECUTOR_SERVICE)
	@ConditionalOnMissingBean(name = BEAN_NAME_EXECUTOR_SERVICE)
	public ScheduledExecutorService controlScheduledExecutorService(
			@Qualifier(BEAN_NAME_THREAD_FACTORY) ThreadFactory controlHandlerThreadFactory
	) {
		return Executors.newScheduledThreadPool(100, controlHandlerThreadFactory);
	}

	@Bean
	public ControlAnnotationHandler controlAnnotationHandler(
			@Qualifier(BEAN_NAME_EXECUTOR_SERVICE) ScheduledExecutorService executorService
	) {
		return new ControlAnnotationHandler(executorService);
	}

	@Bean
	public static ControlAnnotationBeanPostProcessor controlAnnotationBeanPostProcessor(
			ObjectProvider<ControlAnnotationHandler> handler
	) {
		return new ControlAnnotationBeanPostProcessor(handler);
	}
}
