package com.digit.services.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkflowTransitionRequest {

    @JsonProperty("processId")
    private String processId;

    @JsonProperty("entityId")
    private String entityId;

    @JsonProperty("action")
    private String action;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("attributes")
    private Map<String, List<String>> attributes;

    // @Data
    // @AllArgsConstructor
    // @NoArgsConstructor
    // @Builder
    // public static class Attributes {

    //     @JsonProperty("roles")
    //     private List<String> roles = new ArrayList<>();

    //     @JsonProperty("jurisdiction")
    //     private List<String> jurisdiction = new ArrayList<>();
    // }
}
