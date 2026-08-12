package org.egov.web.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import java.util.List;

/**
 * Wire format for a localisation upsert handed to the consumer instead of being applied
 * inline on the request thread.
 *
 * <p>Deliberately separate from {@link CreateMessagesRequest}, which exposes getters only
 * and so serialises out of Jackson but does not reliably deserialise back in.
 *
 * <p>Carries the full {@code RequestInfo} so the consumer rebuilds the same
 * {@code AuthenticatedUser} the synchronous path would have used - audit columns must not
 * change just because the write moved to another thread.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpsertMessagesEvent {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	private String tenantId;

	private List<MessagePayload> messages;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class MessagePayload {
		private String code;
		private String message;
		private String module;
		private String locale;
	}

	/**
	 * Partition key.
	 *
	 * <p>This is the whole point of the layer. Kafka orders only within a partition, and
	 * the defect being fixed is concurrent upserts of the SAME (tenant, locale, module,
	 * code) racing on the unique constraint: two writers both miss the existence check,
	 * both insert, one gets 23505, and the whole chunk is rejected with a 400 - silent
	 * message loss. Keying on tenant+locale+module puts colliding writes on one partition,
	 * where the single consumer applies them in order and the race cannot occur.
	 *
	 * <p>An unkeyed send (the norm elsewhere in DIGIT) would NOT fix this.
	 */
	@JsonIgnore
	public String getPartitionKey() {
		if (messages == null || messages.isEmpty()) {
			return tenantId;
		}
		MessagePayload first = messages.get(0);
		return tenantId + ":" + first.getLocale() + ":" + first.getModule();
	}
}
