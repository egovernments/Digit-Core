package org.egov.infra.indexer.consumer.config;


import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.egov.infra.indexer.consumer.PGRCustomIndexMessageListener;
import org.egov.tracer.KafkaConsumerErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import lombok.extern.slf4j.Slf4j;



@Configuration
@EnableKafka
@PropertySource("classpath:application.properties")
@Order(6)
@Slf4j
public class PGRCustomIndexConsumerConfig implements ApplicationRunner {

	public static KafkaMessageListenerContainer<String, String> kafkContainer;
	
	@Value("${spring.kafka.bootstrap.servers}")
    private String brokerAddress;
	
	@Value("${spring.kafka.consumer.group}")
    private String consumerGroup;
	
	@Value("${egov.indexer.pgr.create.topic.name}")
	private String pgrCreateTopic;
	
	@Value("${egov.indexer.pgr.update.topic.name}")
	private String pgrUpdateTopic;
	
	@Value("${egov.indexer.pgr.legacyindex.topic.name}")
	private String pgrLegacyTopic;

	@Value("${pgr.create.topic.name}")
	private String pgrServicesCreateTopic;

	@Value("${pgr.update.topic.name}")
	private String pgrServicesUpdateTopic;

	@Value("${pgr.legacy.topic.name}")
	private String pgrServicesLegacyTopic;

	@Value("${pgr.batch.create.topic.name}")
	private String pgrServicesBatchCreateTopic;

//	@Autowired
//    private StoppingErrorHandler stoppingErrorHandler;

	@Autowired
	private KafkaConsumerErrorHandler kafkaConsumerErrorHandler;

    @Autowired
    private PGRCustomIndexMessageListener indexerMessageListener;
    
    public String[] topics = {};
    
     
    @Override
    public void run(final ApplicationArguments arg0) throws Exception {
    	try {
				log.info("Starting kafka listener container......");			
				startContainer();
			}catch(Exception e){
				log.error("Exception while Starting kafka listener container: ",e);
			}
    }
    
    public String setTopics(){
    	String[] topics = new String[6];
    	topics[0] = pgrCreateTopic;
    	topics[1] = pgrUpdateTopic;
    	topics[2] = pgrLegacyTopic;
		topics[3] = pgrServicesCreateTopic;
		topics[4] = pgrServicesLegacyTopic;
		topics[5] = pgrServicesBatchCreateTopic;

    	this.topics = topics;  
    	
    	log.info("PGR: Topics intialized..");
    	return topics.toString();
    }
    
    
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "600000");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "300");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(kafkaConsumerErrorHandler);
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(30000);
        
        log.info("Custom KafkaListenerContainerFactory built...");
        return factory;

    }

     
    public KafkaMessageListenerContainer<String, String> container() throws Exception { 
    	 setTopics();
    	 ContainerProperties properties = new ContainerProperties(this.topics); // set more properties
//    	 properties.setPauseEnabled(true);
//    	 properties.setPauseAfter(0);
    	 properties.setMessageListener(indexerMessageListener);
    	 
         log.info("PGR KafkaListenerContainer built...");

         return new KafkaMessageListenerContainer<>(consumerFactory(), properties); 
    }
        
    public boolean startContainer(){
    	KafkaMessageListenerContainer<String, String> container = null;
    	try {
			    container = container();
			    kafkContainer = container;
		} catch (Exception e) {
			log.error("Container couldn't be started: ",e);
			return false;
		}
    	kafkContainer.start();
    	log.info("Custom KakfaListenerContainer STARTED...");    	
    	return true;
    	
    }
    
    public boolean pauseContainer(){
    	try {
        	kafkContainer.stop();
		} catch (Exception e) {
			log.error("Container couldn't be started: ",e);
			return false;
		}	   
    	log.info("Custom KakfaListenerContainer STOPPED...");    	

    	return true;
    }

}
