package ru.drobunind.spring.starter.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import ru.drobunind.spring.starter.control.annotation.Control;
import ru.drobunind.spring.starter.control.annotation.ControlExclude;
import ru.drobunind.spring.starter.control.utils.Reflections;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ControlAnnotationBeanPostProcessor implements BeanPostProcessor {
	private static final Logger logger = LoggerFactory.getLogger(ControlAnnotationBeanPostProcessor.class);

	private final ObjectProvider<ControlAnnotationHandler> controlHandler;
	private final Map<String, Map<MethodKey, AnnotationInfo>> controlledMethods = new ConcurrentHashMap<>();
	private final Map<String, Class<?>> beans = new ConcurrentHashMap<>();

	public ControlAnnotationBeanPostProcessor(ObjectProvider<ControlAnnotationHandler> controlHandler) {
		this.controlHandler = controlHandler;
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		beans.put(beanName, bean.getClass());
		Class<?> beanClass = bean.getClass();

		if (beanClass.getAnnotation(ControlExclude.class) != null) {
			return bean;
		}

		Control beanAnnotation = beanClass.getAnnotation(Control.class);

		Map<MethodKey, AnnotationInfo> annotatedMethods = new ConcurrentHashMap<>();
		Set<MethodKey> set = Reflections.allInterfaces(beanClass).stream()
				.flatMap(c -> Arrays.stream(c.getMethods()))
				.map(ControlAnnotationBeanPostProcessor::toKey)
				.collect(Collectors.toSet());
		List<Method> publicMethods = Arrays.stream(bean.getClass().getMethods())
				.filter(m -> set.contains(toKey(m)))
				.toList();

		for (Method method : publicMethods) {
			if (!Modifier.isPublic(method.getModifiers())) continue;
			MethodKey mKey = toKey(method);
			if (method.getAnnotation(ControlExclude.class) != null) {
				annotatedMethods.put(mKey, new AnnotationInfo(AnnotationLocation.EXCLUDE, null));
				continue;
			}
			Control methodAnnotation = method.getAnnotation(Control.class);
			if (methodAnnotation != null) {
				annotatedMethods.put(mKey, new AnnotationInfo(AnnotationLocation.METHOD, methodAnnotation));
				logApplied(method);
			} else if (beanAnnotation != null) {
				annotatedMethods.put(mKey, new AnnotationInfo(AnnotationLocation.BEAN, beanAnnotation));
				logApplied(method);
			}
		}

		if (!annotatedMethods.isEmpty()) {
			controlledMethods.put(beanName, annotatedMethods);
		}

		return bean;
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (!controlledMethods.isEmpty() && controlledMethods.containsKey(beanName)) {
			var original = beans.get(beanName);
			return Proxy.newProxyInstance(
					original.getClassLoader(),
					Reflections.allInterfaces(original).toArray(Class<?>[]::new),
					new ControlledMethodInterceptor(
							bean,
							beanName,
							controlledMethods.get(beanName),
							controlHandler
					)
			);
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

	private static class ControlledMethodInterceptor implements InvocationHandler {
		private final Object bean;
		private final String beanName;
		private final Map<MethodKey, AnnotationInfo> controlledMethods;
		private final ObjectProvider<ControlAnnotationHandler> controlHandler;
		private final Map<MethodKey, String> controlIdCache = new ConcurrentHashMap<>();

		private ControlledMethodInterceptor(Object bean,
		                                    String beanName,
		                                    Map<MethodKey, AnnotationInfo> controlledMethods,
		                                    ObjectProvider<ControlAnnotationHandler> controlHandler) {
			this.bean = bean;
			this.controlHandler = controlHandler;
			this.beanName = beanName;
			this.controlledMethods = new HashMap<>(controlledMethods);
		}

		private String getControlId(MethodKey mKey, AnnotationInfo info) {
			return controlIdCache.computeIfAbsent(mKey, methodKey -> {
				if (!info.location().equals(AnnotationLocation.METHOD)) {
					return beanName;
				}
				return beanName +
						"." +
						methodKey.name() +
						Arrays.toString(methodKey.parameterTypes());
			});
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			var mKey = toKey(method);
			var info = this.controlledMethods.get(mKey);

			if (info == null || info.location().equals(AnnotationLocation.EXCLUDE)) {
					return method.invoke(bean, args);
			}

			String controlId = getControlId(mKey, info);
			logger.trace("controlId for method [{}] is [{}]", method, controlId);
			try {
				return controlHandler.getObject().invoke(
						() -> method.invoke(bean, args),
						getControlId(mKey, info),
						info
				);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}
	}

	private void logApplied(Method method) {
		logger.trace("Annotation [{}] will be applied to method [{}]", Control.class.getCanonicalName(), method);
	}

	public static MethodKey toKey(Method method) {
		return new MethodKey(method.getName(), method.getParameterTypes());
	}

	// NOTE: not working without custom equals AND hashcode
	public record MethodKey(String name, Class<?>[] parameterTypes) {
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof MethodKey(String oName, Class<?>[] oParameterTypes))) return false;
			if (o == this) return true;
			boolean nameEq = Objects.equals(name, oName);
			boolean paramEq = parameterTypes.length == oParameterTypes.length;
			if (!nameEq || !paramEq) return false;
			for (int i = 0; i < parameterTypes.length; ++i) {
				if (parameterTypes[i] != oParameterTypes[i]) return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, Arrays.hashCode(parameterTypes));
		}
	}
}