package org.egov.autocomplete_workflows.config;

import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ServiceConfiguration {

	@Value("${egov.wf.host}${egov.wf.businessservice.search.endpoint}")
	private String wfBusinessServiceSearchURI;

	@Value("${egov.wf.host}${egov.wf.process.search.endpoint}")
	private String wfProcessSearchURI;

	@Value("${egov.wf.host}${egov.wf.process.transition.endpoint}")
	private String wfProcessTransitionURI;

	@Value("${autocomplete.parent.wf.action.map}")
	private String autocompleteActionMapString;

	private Map<String, String> autocompleteActionMap = new HashMap<>();

	@PostConstruct
	public void init() {
		autocompleteActionMap = Arrays.stream(autocompleteActionMapString.split(","))
				.map(entry -> entry.split(":", 2))
				.filter(pair -> pair.length == 2)
				.collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]));

		// You can now use `myMap` like a regular Map
		System.out.println("Loaded map: " + autocompleteActionMap);
	}
}
