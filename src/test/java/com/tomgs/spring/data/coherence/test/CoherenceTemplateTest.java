package com.tomgs.spring.data.coherence.test;

import javax.annotation.Resource;

import org.junit.Test;

import com.tomgs.spring.data.coherence.support.CoherenceTemplate;

public class CoherenceTemplateTest extends CoherenceBaseTest {
	
	@Resource(name = "coherenceTemplate")
	private CoherenceTemplate<String, Object> coherenceTemplate;
	
	String cacheName1 = "cache-pc-test1";
	String cacheName2 = "cache-pc-test2";
	
	@Test
	public void testPut1() {
		
		String k = "2";
		Object v = "tincopper";
		Object result = coherenceTemplate.opsForValue(cacheName1).put(k, v);
		System.out.println(result);
		System.out.println(coherenceTemplate.opsForValue(cacheName1).get(k));
		
		k = "3";
		v = "tomgs";
		result = coherenceTemplate.opsForValue(cacheName2).put(k, v);
		System.out.println(result);
		System.out.println(coherenceTemplate.opsForValue(cacheName2).get(k));
		
	}
}
