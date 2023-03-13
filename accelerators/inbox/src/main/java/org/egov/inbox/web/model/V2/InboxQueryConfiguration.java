package org.egov.inbox.web.model.V2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InboxQueryConfiguration {

    @JsonProperty("module")
    private String module;

    @JsonProperty("index")
    private String index;

    @JsonProperty("allowedSearchCriteria")
    private List<SearchParam> allowedSearchCriteria;

    @JsonProperty("sortBy")
    private SortParam sortParam;

    @JsonProperty("sourceFilterPathList")
    private List<String> sourceFilterPathList;

}
