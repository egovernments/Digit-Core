package org.egov.infra.persist.service;

import com.github.zafarkhaja.semver.Version;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.egov.infra.persist.repository.PersistRepository;
import org.egov.infra.persist.utils.Utils;
import org.egov.infra.persist.web.contract.JsonMap;
import org.egov.infra.persist.web.contract.Mapping;
import org.egov.infra.persist.web.contract.QueryMap;
import org.egov.infra.persist.web.contract.TopicMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class PersistService {

	@Autowired
	private TopicMap topicMap;

	@Autowired
	private PersistRepository persistRepository;

	@Autowired
	private Utils utils;

	@Transactional
	public void persist(String topic, String json) {

		Map<String, List<Mapping>> map = topicMap.getTopicMap();

		Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
		List<Mapping> applicableMappings = filterMappings(map.get(topic), document);
		log.info("{} applicable configs found!", applicableMappings.size());

		for (Mapping mapping : applicableMappings) {
			List<QueryMap> queryMaps = mapping.getQueryMaps();
			for (QueryMap queryMap : queryMaps) {
				String query = queryMap.getQuery();
				List<JsonMap> jsonMaps = queryMap.getJsonMaps();
				String basePath = queryMap.getBasePath();
				persistRepository.persist(query, jsonMaps, document, basePath);

			}

		}
	}

	/**
	 * Optimized batch persist with query-level row aggregation.
	 *
	 * Instead of executing batchUpdate per message per QueryMap,
	 * this aggregates rows across all messages for each QueryMap
	 * and executes a single batchUpdate per QueryMap.
	 *
	 * Order preservation:
	 * 1. QueryMaps are processed in YAML-defined order (critical for FK dependencies)
	 * 2. Rows within each QueryMap preserve message order (first message's rows first)
	 *
	 * @param topic Kafka topic name
	 * @param jsons List of JSON messages from batch
	 */
	@Transactional
	public void persist(String topic, List<String> jsons) {

		Map<String, List<Mapping>> map = topicMap.getTopicMap();

		// Step 1: Parse all documents and pair with their applicable mappings
		// Using LinkedHashMap to preserve message order
		Map<Object, List<Mapping>> documentToMappings = new LinkedHashMap<>();

		for (String json : jsons) {
			Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
			documentToMappings.put(document, filterMappings(map.get(topic), document));
		}

		// Step 2: Group documents by mapping (using mapping name as key for identity)
		// This handles the case where different messages might have different versions
		Map<String, List<Object>> mappingNameToDocuments = new LinkedHashMap<>();
		Map<String, Mapping> mappingNameToMapping = new LinkedHashMap<>();

		for (Map.Entry<Object, List<Mapping>> entry : documentToMappings.entrySet()) {
			Object document = entry.getKey();
			List<Mapping> mappings = entry.getValue();

			for (Mapping mapping : mappings) {
				String mappingKey = mapping.getName() + ":" + mapping.getVersion();
				mappingNameToDocuments.computeIfAbsent(mappingKey, k -> new ArrayList<>()).add(document);
				mappingNameToMapping.putIfAbsent(mappingKey, mapping);
			}
		}

		// Step 3: For each mapping, process QueryMaps in order with aggregated rows
		for (Map.Entry<String, Mapping> mappingEntry : mappingNameToMapping.entrySet()) {
			String mappingKey = mappingEntry.getKey();
			Mapping mapping = mappingEntry.getValue();
			List<Object> documents = mappingNameToDocuments.get(mappingKey);

			log.info("Processing mapping '{}' with {} documents", mappingKey, documents.size());

			// Process QueryMaps in YAML-defined order (critical for FK/delete-insert order)
			for (QueryMap queryMap : mapping.getQueryMaps()) {
				String query = queryMap.getQuery();
				String basePath = queryMap.getBasePath();
				List<JsonMap> jsonMaps = queryMap.getJsonMaps();

				// Aggregate rows from all documents for this QueryMap
				// Preserves document order: first message's rows appear first
				List<Object[]> aggregatedRows = new ArrayList<>();

				for (Object document : documents) {
					List<Object[]> rows = persistRepository.getRows(jsonMaps, document, basePath);
					aggregatedRows.addAll(rows);
				}

				// Single batchUpdate for all aggregated rows
				if (!aggregatedRows.isEmpty()) {
					log.debug("Executing aggregated batch: {} rows for query starting with '{}'",
							aggregatedRows.size(), query.substring(0, Math.min(50, query.length())));
					persistRepository.persist(query, aggregatedRows);
				}
			}
		}
	}

	private List<Mapping> filterMappings(List<Mapping> mappings, Object json){
		List<Mapping> filteredMaps = new ArrayList<>();
		String version = "";
		try {
			version = JsonPath.read(json, "$.RequestInfo.ver");
		}catch (PathNotFoundException ignore){
		}
		Version semVer = utils.getSemVer(version);
		for (Mapping map: mappings) {
			if(semVer.satisfies(map.getVersion()))
				filteredMaps.add(map);
		}

		return filteredMaps;
	}

}