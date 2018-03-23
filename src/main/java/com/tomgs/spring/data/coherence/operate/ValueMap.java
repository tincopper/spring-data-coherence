package com.tomgs.spring.data.coherence.operate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 处理class的成员变量key-value映射关系
 * @author tomgs
 */
public class ValueMap {
	
	protected final static Logger logger = LoggerFactory.getLogger(ValueMap.class.getClass());
	
	/**
	 * 根据属性值获取序列值<br/>
	 * <b>NOTE:</>不支持获取父类的序列值
	 * @param key	
	 * @param clazz 序列化的类
	 * @return
	 */
	public static int getValue(String key, Class<?> clazz) {
		
		if (StringUtils.isBlank(key)) {
			return -1;
		}
		
		String regex = "[^a-zA-Z]";
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				Object fieldValue = field.get(clazz.newInstance());
				//判断是否为static
				boolean isStatic = Modifier.isStatic(field.getModifiers());
				if (!(fieldValue instanceof Integer && isStatic)) {
					continue;
				}
				int i = (int) fieldValue;
				String name = field.getName();
				if (StringUtils.isBlank(name)) {
					return -1;
				}
				if (StringUtils.equalsIgnoreCase( name.replaceAll(regex, ""), key.replaceAll(regex, "") )) {
					return i;
				}
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				logger.error("获取属性名称为[" + key + "]的失败" , e);
			}
		}
		
		return -1;
	}
	
	/**
	 * 获取实体类中属性名称为{@code key}的值<br/>
	 * <b>NOTE:</b>支持从父类中获取{@code key}
	 * 
	 * @param clazz
	 * @return
	 */
	public static String getId(String key, Object object) {
		
		if (StringUtils.isBlank(key)) {
			return null;
		}
		
		Field field = null;
		String id = null;
		try {
			field = object.getClass().getDeclaredField(key);
		} catch (NoSuchFieldException | SecurityException e) {
			if (!(e instanceof NoSuchFieldException)) {
				logger.error("获取属性名称为[" + key + "]的失败" , e);
				return null;
			}
		}
		if (field == null) {
			try {
				field = object.getClass().getSuperclass().getDeclaredField(key);
			} catch (NoSuchFieldException | SecurityException e1) {
				logger.error("获取key异常,未找到名字为[" + key + "]的属性！" , e1);
				return null;
			}
		}
		try {
			field.setAccessible(true);
			id = String.valueOf(field.get(object));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error("获取属性名称为[" + key + "]的失败" , e);
		}
		return id;
	}
	
	/**
	 * 修改实体{@code object}类属性值
	 * @param object
	 * @param map
	 * @return
	 */
	public static <T> T setValue(T t, Map<String, Object> map) {
		
		Class<?> clazz = t.getClass();
		
		for (Map.Entry<String, Object> entry : map.entrySet()) { 
			
			String filedKey = entry.getKey();
			Object value = entry.getValue();
			
			Field field = null;
			try {
				field = clazz.getDeclaredField(filedKey);
			} catch (NoSuchFieldException | SecurityException e) {
				if (!(e instanceof NoSuchFieldException)) {
					logger.error("获取属性名称为[" + filedKey + "]的失败" , e);
					return null;
				}
			}
			if (field == null) {
				try {
					field = clazz.getSuperclass().getDeclaredField(filedKey);
				} catch (NoSuchFieldException | SecurityException e) {
					logger.error("获取属性名称为[" + filedKey + "]的失败" , e);
					return null;
				}
			}
			field.setAccessible(true);
			try {
				field.set(t, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error("设置属性名称为[" + filedKey + "]的值[" + value + "]失败" , e);
				return null;
			}
		}
		
		return t;
	}
	
}
