package com.tomgs.spring.data.coherence.operate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import com.tangosol.io.pof.reflect.SimplePofPath;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.ValueUpdater;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.extractor.PofUpdater;
import com.tangosol.util.filter.AllFilter;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.LimitFilter;
import com.tangosol.util.processor.UpdaterProcessor;
import com.tomgs.spring.data.coherence.core.processor.BinaryNumberIncrementor;
import com.tomgs.spring.data.coherence.support.CoherenceCallBack;
import com.tomgs.spring.data.coherence.support.CoherenceTemplate;

/**
 * TD: 这里的一层template可以抽象到AbstractValueOpreations
 * @author tomgs
 *
 * @param <K>
 * @param <V>
 */
@SuppressWarnings("unchecked")
public class DefaultOperations<K, V> implements ValueOpreations<K, V> {
	
protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private CoherenceTemplate<K, V> template;
	
	private ThreadLocal<String> cacheNameLocal = new ThreadLocal<String>();
	
	public DefaultOperations(CoherenceTemplate<K, V> template) {
		this.template = template;
	}
	
	public DefaultOperations<K, V> executor(String cacheName) {
		cacheNameLocal.set(cacheName);
		return this;
	}
	
	public V put(final K k, final V v) {
		
		return template.execute(new CoherenceCallBack<V>() {
			@Override
			public V doInCoherence(NamedCache cache) throws DataAccessException {
				
				return (V) cache.put(k, v);
			}
		}, cacheNameLocal);
	}
	
	@Override
	public V put(final K k, final V v, final long expireTime) {
		return template.execute(new CoherenceCallBack<V>() {
			@Override
			public V doInCoherence(NamedCache cache) throws DataAccessException {
				return (V) cache.put(k, v, expireTime);
			}
		}, cacheNameLocal);
	}

	@Override
	public V putIfAbsent(final K k, final V v) {
		
		return template.execute(new CoherenceCallBack<V>() {
			@Override
			public V doInCoherence(NamedCache cache) throws DataAccessException {
				if (cache.get(k) == null) {
					return (V) cache.put(k, v);
				}
				return null;
			}
		}, cacheNameLocal);
	}

	@Override
	public void putAll(final Map<K, V> map) {
		
		template.execute(new CoherenceCallBack<Void>() {
			@Override
			public Void doInCoherence(NamedCache cache) throws DataAccessException {
				cache.putAll(map);
				return null;
			}
		}, cacheNameLocal);
	}

	@Override
	public V get(final K k) {
		
		return template.execute(new CoherenceCallBack<V>() {
			@Override
			public V doInCoherence(NamedCache cache) throws DataAccessException {
				return (V) cache.get(k);
			}
		}, cacheNameLocal);
	}
	
	@Override
	public V getOrDefault(final K k, V defaultValue) {
		
		V result = template.execute(new CoherenceCallBack<V>() {
			@Override
			public V doInCoherence(NamedCache cache) throws DataAccessException {
				return (V) cache.get(k);
			}
		}, cacheNameLocal);
		return null == result ? defaultValue : result;
	}
	
	@Override
	public Map<K, V> getAll(final Collection<K> collection) {
		
		return template.execute(new CoherenceCallBack<Map<K, V>>() {
			@Override
			public Map<K, V> doInCoherence(NamedCache cache) throws DataAccessException {
				return cache.getAll(collection);
			}
		}, cacheNameLocal);
	}

	@Override
	public Set<Entry<K, V>> getEntrySet() {
		
		return template.execute(new CoherenceCallBack<Set<Entry<K, V>>>() {
			@Override
			public Set<Entry<K, V>> doInCoherence(NamedCache cache) throws DataAccessException {
				return cache.entrySet();
			}
		}, cacheNameLocal);
	}

	@Override
	public Set<Entry<K, V>> getEntrySet(final int[] i, final Object value) {
		
		return template.execute(new CoherenceCallBack<Set<Entry<K, V>>>() {
			@Override
			public Set<Entry<K, V>> doInCoherence(NamedCache cache) throws DataAccessException {
				ValueExtractor ve = new PofExtractor(String.class, new SimplePofPath(i));  
				Filter filter = new EqualsFilter(ve, value);  
				Set<Entry<K, V>> set = cache.entrySet(filter);
				return set;
			}
		}, cacheNameLocal);
	}

	@Override
	public Collection<V> getValues() {
		
		return template.execute(new CoherenceCallBack<Collection<V>>() {
			@Override
			public Collection<V> doInCoherence(NamedCache cache) throws DataAccessException {
				return cache.values();
			}
		}, cacheNameLocal);
	}

	@Override
	public Collection<V> getValues(final int[] i, final Object value) {
		
		return template.execute(new CoherenceCallBack<Collection<V>>() {
			@Override
			public Collection<V> doInCoherence(NamedCache cache) throws DataAccessException {
				Collection<V> collections = new ArrayList<V>();
				ValueExtractor ve = new PofExtractor(String.class, new SimplePofPath(i));  
				Filter filter = new EqualsFilter(ve, value);  
				Set<Entry<K, V>> set = cache.entrySet(filter);
				for (Entry<K, V> entry : set) {
					collections.add(entry.getValue());
				}
				return collections;
			}
		}, cacheNameLocal);
	}
	
	@Override
	public Collection<V> getValues(int[] i, Object[] value, int pageSize, int page) {
		
		return getValues(i, value, pageSize, page, null);
	}
	
	public Collection<V> getValues(int[] i, Object[] value, int pageSize, int page, Comparator<V> comparator) {
		
		if (i.length == 0 || value.length == 0 || i.length != value.length) {
			logger.error("parameter error ...");
			return null;
		}
		
		List<Filter> list = new ArrayList<Filter>();
		for (int k = 0; k < i.length; k++) {
			ValueExtractor ve = new PofExtractor(String.class, i[k]);
			Filter equalsFilter = new EqualsFilter(ve, value[k]);
			list.add(equalsFilter);
		}
		
		Filter filter = new AllFilter(list.toArray(new Filter[list.size()]));
		if (pageSize > 0 && page >= 0) {
			filter = new LimitFilter(filter, pageSize);
			((LimitFilter) filter).setPage(page);
			if (comparator != null) {
				((LimitFilter) filter).setComparator(comparator);
			}
		}
		
		final Filter finalFilter = filter;
		return template.execute(new CoherenceCallBack<Collection<V>>() {
			@Override
			public Collection<V> doInCoherence(NamedCache cache) throws DataAccessException {
				Set<Entry<K, V>> set = cache.entrySet(finalFilter);
				
				Collection<V> collections = new ArrayList<V>();
				for (Entry<K, V> entry : set) {
					collections.add(entry.getValue());
				}
				return collections;
			}
		}, cacheNameLocal);
	}
	
	@Override
	public Collection<V> getValues(int[] i, Object[] value) {
		
		return getValues(i, value, 0, 0);
	}
	
	public Collection<V> getValues(int keySeq1, Object value1, int keySeq2, Object value2) {
		
		ValueExtractor ve1 = new PofExtractor(String.class, keySeq1);
		ValueExtractor ve2 = new PofExtractor(String.class, keySeq1);
		
		Filter filterLeft  = new EqualsFilter(ve1, value1);
		Filter filterRight  = new EqualsFilter(ve2, value2);
		Filter filter  = new AndFilter(filterLeft, filterRight);
		
		final Filter finalFilter = filter;
		return template.execute(new CoherenceCallBack<Collection<V>>() {
			@Override
			public Collection<V> doInCoherence(NamedCache cache) throws DataAccessException {
				
				Set<Entry<K, V>> set = cache.entrySet(finalFilter);
				
				Collection<V> collections = new ArrayList<V>();
				for (Entry<K, V> entry : set) {
					collections.add(entry.getValue());
				}
				return collections;
			}
		}, cacheNameLocal);
	}
	
	@Override
	public boolean update(final K keyValue, final int i, final Object targetValue) {
		
		return template.execute(new CoherenceCallBack<Boolean>() {
			@Override
			public Boolean doInCoherence(NamedCache cache) throws DataAccessException {
				ValueUpdater updater = new PofUpdater(i);
				boolean result = (Boolean) cache.invoke(keyValue, new UpdaterProcessor(updater, targetValue));
				return result;
			}
		}, cacheNameLocal);
	}
	
	@Override
	public boolean update(K keyValue, String key, Object value, Class<?> cls) {
		
		//通过key获取序列值
		int i = ValueMap.getValue(key, cls);
		if (i == -1) {
			return false;
		}
		boolean result = update(keyValue, i, value);
		return result;
	}
	
	@Override
	public V upsert(final String keyName, final K keyValue, final String updateKeyName, final Object updateValue, final Class<V> cls) {
		
	    return template.execute(new CoherenceCallBack<V>() {
			@Override
			public V doInCoherence(NamedCache cache) throws DataAccessException {
				
				V v = (V) cache.get(keyValue);
				if (v == null) {
					Map<String, Object> map = new ConcurrentHashMap<String, Object>();
					map.put(keyName, keyValue);
					map.put(updateKeyName, updateValue);
					
					try {
						v = ValueMap.setValue(cls.newInstance(), map);
						cache.put(keyValue, v);
					} catch (InstantiationException | IllegalAccessException e) {
						logger.error("更新操作失败", e);
					}
					return v;
				}
				if (updateValue instanceof Number) {
					Number n = (Number) updateValue;
					
					//increment(keyValue, updateKeyName, n, true, cls, false);
					int fieldSerial = ValueMap.getValue(updateKeyName, cls);
					final BinaryNumberIncrementor incrementor = new BinaryNumberIncrementor(n, true, fieldSerial);
					cache.invoke(keyValue, incrementor);
					
					return (V) cache.get(keyValue);
				}
				//进行更新
				int keySerial = ValueMap.getValue(keyName, cls);
				int updateKeySerial = ValueMap.getValue(updateKeyName, cls);
				
				ValueExtractor key = new PofExtractor(String.class, keySerial);
			    Filter filter = new EqualsFilter(key, keyValue); 
			    ValueUpdater updater = new PofUpdater(updateKeySerial); 
			    
			    cache.invoke(filter, new UpdaterProcessor(updater, updateValue)); 
			    
				return (V) cache.get(keyValue);
			}
		}, cacheNameLocal);
	}
	
	@Override
	public boolean findAndSet(int keySerial, K keyValue, int updateKeySerial, 
			Object value, final Object updateValue) {
		
		ValueExtractor key = new PofExtractor(String.class, keySerial);
		ValueExtractor update = new PofExtractor(String.class, updateKeySerial); 
		
	    Filter leftFilter = new EqualsFilter(key, keyValue); 
	    Filter rightFilter = new EqualsFilter(update, value);
	    final Filter filter  = new AndFilter(leftFilter, rightFilter);
	    
	    final ValueUpdater updater = new PofUpdater(updateKeySerial);  
	    Map<K, V> result = template.execute(new CoherenceCallBack<Map<K, V>>() {
			@Override
			public Map<K, V> doInCoherence(NamedCache cache) throws DataAccessException {
				return cache.invokeAll(filter, new UpdaterProcessor(updater, updateValue));  
			}
		}, cacheNameLocal);
	    
	    if (result.size() <= 0) {
	    	return false;
	    }
	    return result.get(keyValue) == null ? false : true;
	}

	@Override
	public V remove(final K k) {
		
		return template.execute(new CoherenceCallBack<V>() {
			@Override
			public V doInCoherence(NamedCache cache) throws DataAccessException {
				V result = (V) cache.remove(k);
				return result;
			}
		}, cacheNameLocal);
	}
	
	/**
	 * @Deprecated pelease use updateValues(K k, Map<String, Object> keyValues, Class<?> cls) method
	 */
	@Override
	@Deprecated
	public boolean updateValues(K k, Map<String, Object> keyValues, long waitTime) {
		return false;
	}

	@Override
	@Deprecated
	public boolean updateValues(K k, Map<String, Object> keyValues) {
		//30s没有获取到锁则返回更新失败
		return updateValues(k, keyValues, 30000);
	}
	
	@Override
	public boolean updateValues(final K k, final Map<String, Object> keyValues, final Class<?> cls) {
		
		return template.execute(new CoherenceCallBack<Boolean>() {
			@Override
			public Boolean doInCoherence(NamedCache cache) throws DataAccessException {
				
				for (Map.Entry<String, Object> entry : keyValues.entrySet()) {  
					String filedKey = entry.getKey();
					Object value = entry.getValue();
					int filedIndex = ValueMap.getValue(filedKey, cls);
					if (filedIndex == -1) {
						continue;
					}
					ValueUpdater updater = new PofUpdater(filedIndex);
					boolean result = (Boolean) cache.invoke(k, new UpdaterProcessor(updater, value));
					if (!result) {
						logger.error("对应的key值[{}],更新为[{}]失败", filedKey, value);
						return result;
					}
				}
				
				return true;
			}
		}, cacheNameLocal);
		
	}
	
	@Override
	public void clear() {
		
		template.execute(new CoherenceCallBack<Void>() {
			@Override
			public Void doInCoherence(NamedCache cache) throws DataAccessException {
				cache.clear();
				return null;
			}
		}, cacheNameLocal);
	}
	
	@Override
	public Number increment(final K k, String incFieldName, Number n, boolean isRet, Class<?> cls) {

		int fieldSerial = ValueMap.getValue(incFieldName, cls);
		final BinaryNumberIncrementor incrementor = new BinaryNumberIncrementor(n, isRet, fieldSerial);
		return template.execute(new CoherenceCallBack<Number>() {
			@Override
			public Number doInCoherence(NamedCache cache) throws DataAccessException {
				Number retNumber = (Number) cache.invoke(k, incrementor);
				return retNumber;
			}
		}, cacheNameLocal);
	}

	@Override
	public boolean isEmpty() {
		return template.execute(new CoherenceCallBack<Boolean>() {
			@Override
			public Boolean doInCoherence(NamedCache cache) throws DataAccessException {
				return cache.isEmpty();
			}
		}, cacheNameLocal);
	}

}
