package com.github.johrstrom.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

public abstract class CollectorElement<C extends BaseCollectorConfig> extends AbstractTestElement {

	public static final String COLLECTOR_DEF = "johrstrom.collector_definitions";
	
	protected Map<String,Collector> collectors = new HashMap<String,Collector>();
	
	private static Logger log = LoggerFactory.getLogger(CollectorElement.class);
	private static final long serialVersionUID = 963612021269632269L;
	
	public CollectorElement() {
		this.setCollectorDefinitions(new ArrayList<C>());
	}
	
	public CollectionProperty getCollectorConfigs() {
		JMeterProperty collectorDefinitions = this.getProperty(COLLECTOR_DEF);
		
		if(collectorDefinitions == null || collectorDefinitions instanceof NullProperty) {
			collectorDefinitions = new CollectionProperty(COLLECTOR_DEF, new ArrayList<C>());
			collectorDefinitions.setName(COLLECTOR_DEF);
		}
		
		return (CollectionProperty) collectorDefinitions;
		
	}
	
	public void setCollectorDefinitions(List<C> collectors) {
		this.setProperty(new CollectionProperty(COLLECTOR_DEF, collectors));
		this.makeNewCollectors();
	}
	
	protected void makeNewCollectors() {
		for (Entry<String, Collector> entry : this.collectors.entrySet()) {
			CollectorRegistry.defaultRegistry.unregister(entry.getValue());
		}
		
		this.collectors.clear();
		
		CollectionProperty collectorDefs = this.getCollectorConfigs();
		PropertyIterator iter = collectorDefs.iterator();
		
		while(iter.hasNext()) {
			
			try {
				@SuppressWarnings("unchecked")
				C config = (C) iter.next().getObjectValue();
				Collector collector = BaseCollectorConfig.fromConfig(config);
				
				collector.register(CollectorRegistry.defaultRegistry);
				this.collectors.put(config.getMetricName(), collector);
			}catch(Exception e) {
				log.error("Didn't register collector because of error",e);
			}
			
		}
		
	}
	
	
}
