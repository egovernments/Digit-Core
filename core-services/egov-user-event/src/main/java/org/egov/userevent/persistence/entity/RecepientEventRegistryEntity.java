package org.egov.userevent.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Maps eg_usrevents_recepnt_event_registry. The table has no primary key;
 * the (recepient, eventid) pair is used as a composite JPA id — the write
 * path never produces duplicate pairs for one event.
 */
@Entity
@Table(name = "eg_usrevents_recepnt_event_registry")
@IdClass(RecepientEventRegistryId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecepientEventRegistryEntity {

	@Id
	private String recepient;

	@Id
	@Column(name = "eventid")
	private String eventId;
}
