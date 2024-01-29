package org.egov.infra.indexer.consumer.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.apache.kafka.clients.consumer.Consumer;
//import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.stereotype.Component;

@Component
public class StoppingErrorHandler implements CommonErrorHandler {

	@Autowired
	private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

	@Override
	public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer,
			MessageListenerContainer container) {
		try {
			kafkaListenerEndpointRegistry.stop();
			return true;
		} catch (Exception ex) {
			return false;
		}
		// return CommonErrorHandler.super.handleOne(thrownException, record, consumer,
		// container);
	}

	@Override
	public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer,
			MessageListenerContainer container, boolean batchListener) {
		CommonErrorHandler.super.handleOtherException(thrownException, consumer, container, batchListener);
	}

//  @Override
//  public void handle(Exception thrownException, ConsumerRecord<?, ?> record) {
//    kafkaListenerEndpointRegistry.stop();
//  }

}
