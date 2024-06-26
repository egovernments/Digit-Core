package org.selco.e4h.config;

import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TracerConfiguration.class})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConsumerConfiguration {

	//Kafka Config
	@Value("${kafka.config.bootstrap_server_config}")
	private String brokerAddress;

	@Value("${spring.kafka.consumer.group-id}")
	private String consumerGroup;

	@Value("${kafka.topics.consumer}")
	private String consumerTopics;

	//ElasticSearch Config
	@Value("${egov.infra.indexer.host}")
	private String esHostUrl;

	@Value("${egov.update.index.path}")
	private String updateIndexPath;

	@Value("${elasticsearch.poll.interval.seconds}")
	private String pollInterval;

	@Value("${egov.indexer.es.username}")
	private String esUsername;

	@Value("${egov.indexer.es.password}")
	private String esPassword;

	@Value("${egov.statelevel.tenantId}")
	private String stateLevelTenantId;
}

