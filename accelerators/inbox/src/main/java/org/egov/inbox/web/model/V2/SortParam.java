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
public class SortParam {

    @JsonProperty("path")
    private String path;

    @JsonProperty("defaultOrder")
    private Order order;

    public enum Order {

        ASC("ASC"),

        DESC("DESC");

        private String value;

        Order(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static Order fromValue(String text) {
            for (Order b : Order.values()) {
                if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }


}
