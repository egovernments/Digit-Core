package org.egov.userevent.persistence.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecepientEventRegistryId implements Serializable {

	private String recepient;

	private String eventId;
}
