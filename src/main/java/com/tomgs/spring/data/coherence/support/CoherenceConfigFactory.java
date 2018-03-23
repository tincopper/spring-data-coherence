package com.tomgs.spring.data.coherence.support;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;

/**
 * coherence工厂
 * @author evelyn
 *
 */
public class CoherenceConfigFactory implements InitializingBean, DisposableBean {
	
	private final static Logger logger = LoggerFactory.getLogger(CoherenceConfigFactory.class); 
	
	private ConfigurableCacheFactory factory;
	
	private String configName;

	@Override
	public void destroy() throws Exception {
		if (factory != null) {
			try {
				factory = null;
		        CacheFactory.shutdown();
			} catch (Exception ex) {
				logger.warn("factory set null & CacheFactory.shutdown", ex);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(getConfigName(), "cachConfigName is required");
		this.factory=createFactory();
	}
	
	private ConfigurableCacheFactory createFactory() {

		if (isConfigAware()) {
			return createUserConfigPool(this.configName);
		}
		return createDefalutFactory();
	}
	
	protected ConfigurableCacheFactory createDefalutFactory() {

		return CacheFactory.getCacheFactoryBuilder().getConfigurableCacheFactory("spring-embedded-coherence-config.xml", null);
	}
	
	protected ConfigurableCacheFactory createUserConfigPool(String configName) {

		return CacheFactory.getCacheFactoryBuilder().getConfigurableCacheFactory(configName, null);
	}
	
	public CoherenceConfigFactory(String configName){
		this.configName=configName;
	}
	
	public CoherenceConfigFactory(){
	}
	
	/**
	 * 是否指定配置文件
	 * @return
	 */
	public boolean isConfigAware() {
		return StringUtils.isNotEmpty(configName);
	}

	/**
	 * @return the configName
	 */
	public String getConfigName() {
		return configName;
	}

	/**
	 * @param configName the configName to set
	 */
	public void setConfigName(String configName) {
		this.configName = configName;
	}

}
