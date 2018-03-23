package com.tomgs.spring.data.coherence.operate;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Coherence 缓存操作接口
 * @author tomgs
 *
 * @param <K>
 * @param <V>
 */
public interface ValueOpreations<K, V> {
	
	/**
	 * put {@code v} for {@code k}
	 * @param k key must not be {@literal null}
	 * @param v value
	 * @return
	 */
	V put(K k, V v);
	
	/**
	 * 插入缓存数据并设置过期时间
	 * @param k  key
	 * @param v  value
	 * @param expireTime 过期时间（ms）
	 * @return
	 */
	V put(K k, V v, long expireTime);
	
	/**
	 * 将值{@code v} 设置到 {@code k}中，如果缓存中存在，则返回null，否则返回插入的结果{@code v}
	 * @param k
	 * @param v
	 * @return
	 */
	V putIfAbsent(K k, V v);
	
	/**
	 * 批量插入
	 * @param map
	 */
	void putAll(Map<K, V> map);
	
	/**
	 * 获取指定{@code k} 的数据
	 * @param k
	 * @return v
	 */
	V get(K k);
	
	/**
	 * 获取指定{@code k} 的数据，未获取到则返回{@code defaultValue}
	 * @param k
	 * @param defaultValue
	 * @return
	 */
	V getOrDefault(K k, V defaultValue);
	
	/**
	 * 获取所有指定的{@code k}的数据
	 * @param collection
	 * @return
	 */
	Map<K, V> getAll(Collection<K> collection);
	
	/**
	 * 获取指定缓存中所有数据集合,返回包括key和 value
	 * @return
	 */
	Set<Entry<K, V>> getEntrySet();
	
	/**
	 * 复合对象中进行查询，如new int[] {Contact.HOME_ADDRESS, Address.ZIP}，</br>
	 * 则查询的是Contact对象成员变量Address中zip的值为{@code value}的数据
	 * @param k  复合对象序列值
	 * @param i  zip的值
	 * @return
	 */
	Set<Entry<K, V>> getEntrySet(int[] i, Object value);
	
	/**
	 * 获取指定缓存中所有数据的value,返回不包括key
	 * @return value的集合
	 */
	Collection<V> getValues();
	
	/**
	 * 获取指定key的value,返回不包括key
	 * @return value的集合
	 */
	Collection<V> getValues(int[] i, Object value);
	
	/**
	 * 获取指定key的value,返回不包括key
	 * @return 分页获取value的集合
	 */
	Collection<V> getValues(int[] i, Object[] value, int pageSize, int page);
	
	/**
	 * 获取指定key的value,返回不包括key
	 * @param i
	 * @param value
	 * @param pageSize
	 * @param page
	 * @param comparator 排序器
	 * @return 分页获取value的集合
	 */
	public Collection<V> getValues(int[] i, Object[] value, int pageSize, int page, Comparator<V> comparator);
	
	/**
	 * 获取指定key的value,返回不包括key
	 * @return value的集合
	 */
	Collection<V> getValues(int[] i, Object[] value);
	
	/**
	 * 更新数据
	 * @param keyValue	插入时key的值
	 * @param i			要更新的对象序列值
	 * @param targetValue 要更新的目标值
	 * @return
	 */
	boolean update(K keyValue, int i, Object targetValue);
	
	/**
	 * 更新数据
	 * @param id
	 * @param key
	 * @param value
	 * @param cls 对象序列化类
	 * @return
	 */
	boolean update(K keyValue, String key, Object value, Class<?> cls);
	
	/**
	 * 一次更新对象中的多个值</br>
	 * <b>注意：</b>30s没有获取到锁则返回更新失败，并且这是一个线程安全的更新操作
	 * @param id 插入时对象的key
	 * @param keyValues 要更新对象属性的key-value
	 * @return
	 */
	public boolean updateValues(K k, Map<String, Object> keyValues);
	
	/**
	 *  一次更新对象中的多个值</br>
	 *  这是一个线程安全的更新操作
	 * @param k	插入时对象的key
	 * @param keyValues 要更新对象属性的key-value
	 * @param waitTime	更新等待时间(ms)，{@code waitTime}毫秒，没有更新则返回更新失败
	 * @return
	 */
	public boolean updateValues(K k, Map<String, Object> keyValues, long waitTime);
	
	/**
	 * 一次更新对象中的多个值
	 * @param k
	 * @param keyValues
	 * @param cls 序列化类class
	 * @return
	 */
	boolean updateValues(K k, Map<String, Object> keyValues, Class<?> cls);
	
	/**
	 * 如果有数据则更新如果没有则插入数据
	 * @param keySerial
	 * @param keyValue
	 * @param updateSerial
	 * @param oldValue
	 * @param updateValue
	 * @return
	 */
	V upsert(String keyName, K keyValue, String updateKeyName, Object updateValue, Class<V> cls);
	/**
	 * 先查找符合的数据再更新
	 * @param keySerial  查找数据的key的序列值（字段）
	 * @param keyValue	  查找数据的key的值
	 * @param updateSerial 要更新的数据的序列值（字段）
	 * @param oldValue	更新之前的值
	 * @param updateValue  要更新的数据的值
	 * @return
	 */
	boolean findAndSet(int keySerial, K keyValue, int updateSerial, Object oldValue, Object updateValue);
	
	/**
	 * 数值字段增加操作，并返回增加后的结果值</br>
	 * <b>NOTE:</b>此增加方法线程不安全，线程安全请使用{@link increment(K k, String incFieldName, Number n, boolean isRet, Class<?> cls) }</br>
	 * @param k	
	 * @param incFieldName	需要数值操作的 属性名称
	 * @param n	增加值
	 * @return
	 */
	//Number increment(K k, String incFieldName, Number n);
	
	/**
	 * 数值字段增加操作，并返回增加后的结果值</br>
	 * <b>NOTE:</b>此增加方法线程安全
	 * @param k	
	 * @param incFieldName	需要数值操作的 属性名称
	 * @param n	增加值
	 * @param isRet true:返回增加之后的值，false：返回增加之前的值
	 * @return 
	 */
	Number increment(K k, String incFieldName, Number n, boolean isRet, Class<?> cls);
	
	/**
	 * 删除key为{@code k} 的值
	 * @param k
	 * @return
	 */
	V remove(K k);
	
	/**
	 * 清空缓存
	 */
	void clear();
	
	/**
	 * 当前cache是否为空
	 * @return
	 */
	boolean isEmpty();
	
	/**
	 * 对{@code k}加锁
	 * @param k
	 * @return
	 */
	//boolean lock(K k);
	
	/**
	 * 对{@code k}进行加锁操作
	 * @param k	要锁定的值
	 * @param waitTime  获取锁的毫秒数; 0 ：立即返回，不管是否获得锁; -1 ：阻止调用线程，直到可以获得锁
	 * @return 是否获取{@code k}的锁成功
	 */
	//boolean lock(K k, long waitTime);
	
	/**
	 * 对{@code k}解锁
	 * @param k
	 * @return
	 */
	//boolean unlock(K k);

}
