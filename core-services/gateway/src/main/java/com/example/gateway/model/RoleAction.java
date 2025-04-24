package com.example.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RoleAction {

	@JsonProperty("url")
	private String url;

	@JsonProperty("id")
	private long id;

	private String boundaryType;

	private String hierarchyType;

}
