package org.egov.autocomplete_workflows.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.autocomplete_workflows.config.ServiceConfiguration;
import org.egov.autocomplete_workflows.models.ProcessInstance;
import org.egov.autocomplete_workflows.models.ProcessInstanceRequest;
import org.egov.autocomplete_workflows.service.AutocompleteService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class AutocompleteConsumer {

	private final ObjectMapper mapper;

	private final AutocompleteService autocompleteService;

	private final ServiceConfiguration serviceConfig;

	public AutocompleteConsumer(ObjectMapper mapper, AutocompleteService autocompleteService, ServiceConfiguration serviceConfig) {
		this.mapper = mapper;
		this.autocompleteService = autocompleteService;
		this.serviceConfig = serviceConfig;
	}

	@KafkaListener(topics = {"${kafka.topics.consumer}"})
	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		try {
			ProcessInstanceRequest processInstanceRequest = mapper.convertValue(record, ProcessInstanceRequest.class);

			if (processInstanceRequest == null || processInstanceRequest.getProcessInstances() == null) {
				log.warn("Received empty or invalid ProcessInstanceRequest");
				return;
			}

			for (ProcessInstance processInstance : processInstanceRequest.getProcessInstances()) {
				String action = processInstance.getAction();
				if (action != null && serviceConfig.getAutocompleteActionMap().containsKey(action)) {
					autocompleteService.terminateParallelWorkflows(processInstanceRequest.getRequestInfo(), processInstance);
				}
			}
		} catch (Exception e) {
			log.error("Error processing Kafka record from topic {}: {}", topic, e.getMessage(), e);
		}
	}
}
