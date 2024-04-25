package org.egov.sunbirdrc.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MdmsSchema {
    private MdmsData mdmsData;
    private Schema schema;
    private List<String> tags;
    private String status;
}

