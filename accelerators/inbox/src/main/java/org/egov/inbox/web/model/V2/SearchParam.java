package org.egov.inbox.web.model.V2;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SearchParam {

    @JsonProperty("name")
    private String name;

    @JsonProperty("path")
    private String path;

    @JsonProperty("isMandatory")
    private Boolean isMandatory;

    @JsonProperty("isHashingRequired")
    private Boolean isHashingRequired;

    @JsonProperty("operator")
    private Operator operator;

    public enum Operator {

        EQUAL("EQUAL"),

        GTE("GTE"),

        LTE("LTE"), 
        
        WILDCARD("WILDCARD");


        private String value;

        Operator(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static Operator fromValue(String text) {
            for (Operator b : Operator.values()) {
                if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }


}
