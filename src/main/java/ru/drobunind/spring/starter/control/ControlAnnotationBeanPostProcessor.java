package ru.drobunind.spring.starter.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import ru.drobunind.spring.starter.control.annotation.Control;
import ru.drobunind.spring.starter.control.annotation.ControlExclude;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ControlAnnotationBeanPostProcessor implements BeanPostProcessor {
	private static final Logger logger = LoggerFactory.getLogger(ControlAnnotationBeanPostProcessor.class);

	private final ObjectProvider<ControlAnnotationHandler> controlHandler;
	private final Map<String, Map<Method, AnnotationInfo>> controlledMethods = new ConcurrentHashMap<>();

	public ControlAnnotationBeanPostProcessor(ObjectProvider<ControlAnnotationHandler> controlHandler) {
		this.controlHandler = controlHandler;
	}

	private void logApplied(Method method) {
		logger.trace("Annotation [{}] will be applied to method [{}]", Control.class.getCanonicalName(), method);
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Class<?> beanClass = bean.getClass();

		Control beanAnnotation = beanClass.getAnnotation(Control.class);
		if (beanClass.getAnnotation(ControlExclude.class) != null) {
			return bean;
		}
		Map<Method, AnnotationInfo> annotatedMethods = new ConcurrentHashMap<>();
		for (Method method : beanClass.getDeclaredMethods()) {
			if (!Modifier.isPublic(method.getModifiers())) continue;
			if (method.getAnnotation(ControlExclude.class) != null) {
				annotatedMethods.put(method, new AnnotationInfo(AnnotationLocation.EXCLUDE, null));
				continue;
			}
			Control methodAnnotation = method.getAnnotation(Control.class);
			if (methodAnnotation != null) {
				annotatedMethods.put(method, new AnnotationInfo(AnnotationLocation.METHOD, methodAnnotation));
				logApplied(method);
			} else if (beanAnnotation != null) {
				annotatedMethods.put(method, new AnnotationInfo(AnnotationLocation.BEAN, beanAnnotation));
				logApplied(method);
			}
		}

		if (!annotatedMethods.isEmpty()) {
			controlledMethods.put(beanName, annotatedMethods);
		}

		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (!controlledMethods.isEmpty() && controlledMethods.containsKey(beanName)) {
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(bean.getClass());
			enhancer.setCallback(new ControlledMethodInterceptor(
							beanName,
							controlledMethods.remove(beanName),
							controlHandler
					)
			);
			return enhancer.create();
		}
		return bean;
	}

	public enum AnnotationLocation {
		BEAN,
		METHOD,
		EXCLUDE
	}

	public record AnnotationInfo(AnnotationLocation location, Control annotation) {
	}

	private static class ControlledMethodInterceptor implements MethodInterceptor {
		private final String beanName;
		private final Map<Method, AnnotationInfo> controlledMethods;
		private final ObjectProvider<ControlAnnotationHandler> controlHandler;
		private final Map<Method, String> controlIdCache = new ConcurrentHashMap<>();

		private ControlledMethodInterceptor(String beanName,
		                                    Map<Method, AnnotationInfo> controlledMethods,
		                                    ObjectProvider<ControlAnnotationHandler> controlHandler) {
			this.controlHandler = controlHandler;
			this.beanName = beanName;
			this.controlledMethods = controlledMethods;
		}

		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			var info = controlledMethods.get(method);

			if (info == null || info.location().equals(AnnotationLocation.EXCLUDE)) {
				return proxy.invokeSuper(obj, args);
			}

			String controlId = getControlId(method, info);
			logger.trace("controlId for method [{}] is [{}]", method, controlId);
			return controlHandler.getObject().invoke(
					() -> proxy.invokeSuper(obj, args),
					getControlId(method, info),
					info
			);
		}

		private String getControlId(Method method, AnnotationInfo info) {
			return controlIdCache.computeIfAbsent(method, m -> {
				if (!info.location().equals(AnnotationLocation.METHOD)) {
					return beanName;
				}
				return beanName +
						"." +
						m.getName() +
						Arrays.toString(m.getParameterTypes());
			});
		}
	}
}