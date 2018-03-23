package com.tomgs.spring.data.coherence.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 指定缓存读取类
 * @author evelyn
 *
 */
public class CoherenceAccessor implements InitializingBean {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	 /**
     * 配置緩存工厂类
     */
    private  CoherenceConfigFactory factory;
    
	@Override
	public void afterPropertiesSet() throws Exception {
		if (!isConfigFactory()) {
			logger.warn("factory is null!");
		}
	}

	/**
	 * @return the factory
	 */
	public CoherenceConfigFactory getFactory() {
		return factory;
	}

	/**
	 * @param factory the factory to set
	 */
	public void setFactory(CoherenceConfigFactory factory) {
		this.factory = factory;
	}
	
	protected boolean isConfigFactory(){
		return factory != null;
	}

}
