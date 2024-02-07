package org.egov.infra.indexer.consumer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.stereotype.Component;

@Component
public class StoppingErrorHandler implements CommonErrorHandler {

  @Autowired
  private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
  
//  @Override
//  public void handle(Exception thrownException, ConsumerRecord<?, ?> record) {
//    kafkaListenerEndpointRegistry.stop();
//  }




}
