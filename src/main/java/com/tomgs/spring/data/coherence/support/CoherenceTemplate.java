package com.tomgs.spring.data.coherence.support;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tomgs.spring.data.coherence.operate.DefaultOperations;

public class CoherenceTemplate<K, V> extends CoherenceAccessor {
	
	private DefaultOperations<K, V> defOperations;
	
	public DefaultOperations<K, V> opsForValue(String cacheName) {
		
		if (defOperations == null) {
			defOperations = new DefaultOperations<K, V>(this).executor(cacheName);
			return defOperations;
		}
		return defOperations.executor(cacheName);
	}
	
	@Deprecated
	public <T> T execute(CoherenceCallBack<T> action, String cacheName) {
		
		NamedCache cache = CacheFactory.getCache(cacheName);
		try {
			postPreProcess();
			//执行操作
			T result = action.doInCoherence(cache);
			//处理返回结
			return postResultProcess(result);
		} finally {
			CacheFactory.releaseCache(cache);
		}
    }
	
	public <T> T execute(CoherenceCallBack<T> action, ThreadLocal<String> cacheName) {
		
		NamedCache cache = CacheFactory.getCache(cacheName.get());
		try {
			postPreProcess();
			//执行操作
			T result = action.doInCoherence(cache);
			//处理返回结
			return postResultProcess(result);
		} finally {
			CacheFactory.releaseCache(cache);
			//清除ThreadLocal
			cacheName.remove();
		}
    }
	
	private void postPreProcess() {
		
	}

	public <T> T postResultProcess(T result) {
		return result;
	}
}
