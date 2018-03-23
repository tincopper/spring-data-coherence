package com.tomgs.spring.data.coherence.support;

import org.springframework.dao.DataAccessException;

import com.tangosol.net.NamedCache;

public interface CoherenceCallBack<T> {
	
	T doInCoherence(NamedCache connection) throws DataAccessException;
}
