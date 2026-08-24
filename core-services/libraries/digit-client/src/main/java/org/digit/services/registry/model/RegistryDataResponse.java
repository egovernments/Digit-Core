package org.digit.services.registry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.digit.util.DigitJson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The registry service's response envelope.
 *
 * <p>{@code data} is untyped because the same envelope carries a single record, a list of records, a
 * schema, or a small map like {@code {"exists": true}} depending on the endpoint. The typed
 * accessors below convert it for the common cases.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryDataResponse {
    @JsonProperty(value="success")
    private Boolean success;
    @JsonProperty(value="data")
    private Object data;
    @JsonProperty(value="error")
    private String error;
    @JsonProperty(value="message")
    private String message;
    /**
     * The HTTP status the call returned, set by the client rather than the service.
     *
     * <p>Needed because a data write answers 201 normally but 202 with a null {@code data} when the
     * service is configured to persist asynchronously — otherwise indistinguishable from a write
     * that returned nothing.
     */
    @JsonIgnore
    private Integer httpStatus;

    /** True when the write was queued rather than applied, i.e. the service answered 202. */
    @JsonIgnore
    public boolean isQueued() {
        return this.httpStatus != null && this.httpStatus == 202;
    }

    /** {@code data} as a single record, or null when there is none. */
    @JsonIgnore
    public RegistryRecord getRecord() {
        if (this.data == null) {
            return null;
        }
        if (this.data instanceof List<?> list) {
            return list.isEmpty() ? null : DigitJson.shared().convertValue(list.get(0), RegistryRecord.class);
        }
        return DigitJson.shared().convertValue(this.data, RegistryRecord.class);
    }

    /** {@code data} as a list of records; a single record becomes a one-element list. */
    @JsonIgnore
    public List<RegistryRecord> getRecords() {
        if (this.data == null) {
            return List.of();
        }
        if (this.data instanceof List<?>) {
            return DigitJson.shared().convertValue(this.data,
                    DigitJson.shared().getTypeFactory().constructCollectionType(List.class, RegistryRecord.class));
        }
        RegistryRecord single = getRecord();
        return single == null ? List.of() : List.of(single);
    }

    /** {@code data} as a schema, for the schema endpoints. */
    @JsonIgnore
    public RegistrySchema getSchema() {
        return this.data == null ? null : DigitJson.shared().convertValue(this.data, RegistrySchema.class);
    }
}
