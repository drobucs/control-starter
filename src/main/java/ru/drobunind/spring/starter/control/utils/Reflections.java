package ru.drobunind.spring.starter.control.utils;


import java.util.HashSet;
import java.util.Set;

public class Reflections {
	private Reflections() {}

	public static Set<Class<?>> allInterfaces(Class<?> clazz) {
		Set<Class<?>> interfaces = new HashSet<>();

		for (Class<?> intf : clazz.getInterfaces()) {
			interfaces.add(intf);
			interfaces.addAll(allInterfaces(intf));
		}

		Class<?> superClass = clazz.getSuperclass();
		if (superClass != null) {
			interfaces.addAll(allInterfaces(superClass));
		}

		return interfaces;

	}
}
