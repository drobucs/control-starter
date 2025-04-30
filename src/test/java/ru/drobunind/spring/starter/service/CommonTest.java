package ru.drobunind.spring.starter.service;


import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import ru.drobunind.spring.starter.cases.blocking.BlockingMethod;
import ru.drobunind.spring.starter.cases.clazz.ThreeMethods;
import ru.drobunind.spring.starter.cases.exception.ExceptionMethodImpl;
import ru.drobunind.spring.starter.cases.method.MethodImpl;
import ru.drobunind.spring.starter.control.ControlAnnotationBeanPostProcessor.MethodKey;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static ru.drobunind.spring.starter.control.ControlAnnotationBeanPostProcessor.toKey;

@SpringBootTest
public class CommonTest {


	private static final Logger log = LoggerFactory.getLogger(CommonTest.class);

	private void test(Class<?> clazz) {
		Method m1 = clazz.getMethods()[0];
		Method m2 = clazz.getMethods()[1];
		test(m1, m2);
	}

	private void test(Class<?> clazz1, Class<?> clazz2) {
		Method m1 = clazz1.getMethods()[0];
		Method m2 = clazz2.getMethods()[0];
		test(m1, m2);
	}

	private void test(Method m1, Method m2) {
		var mKey1 = toKey(m1);
		var mKey2 = toKey(m2);
		log.info("hash m1: {}, hash m2: {}", mKey1.hashCode(), mKey2.hashCode());
		assertNotEquals(mKey1, mKey2);
		Set<MethodKey> set = new HashSet<>();
		set.add(mKey1);
		set.add(mKey2);
		assertEquals(2, set.size());
		assertTrue(set.contains(mKey1));
		assertTrue(set.contains(mKey2));
		Map<MethodKey, String> map = new HashMap<>();
		map.put(mKey1, "1");
		map.put(mKey2, "2");
		assertTrue(map.containsKey(mKey1));
		assertTrue(map.containsKey(mKey2));
		assertNotNull(map.get(mKey1));
		assertNotNull(map.get(mKey2));
	}

	@Test
	public void testEquals() {
		test(MethodImpl.class);
		test(ExceptionMethodImpl.class);
		test(ThreeMethods.class);
		test(BlockingMethod.class, MethodImpl.class);
		test(ThreeMethods.class, MethodImpl.class);
	}
}
