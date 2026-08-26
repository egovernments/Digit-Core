package org.egov.userevent.persistence.entity;

import org.egov.userevent.web.contract.Action;
import org.egov.userevent.web.contract.EventDetails;
import org.egov.userevent.web.contract.Recepient;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Maps eg_usrevents_events. The three jsonb columns hold the same
 * Jackson-serialized contract objects the Kafka persister used to write, so
 * rows created by either path are interchangeable (the read side keeps
 * deserializing them with the existing row mapper).
 */
@Entity
@Table(name = "eg_usrevents_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventEntity {

	@Id
	private String id;

	@Column(name = "tenantid")
	private String tenantId;

	private String source;

	@Column(name = "eventtype")
	private String eventType;

	private String category;

	private String name;

	private String description;

	private String status;

	@Column(name = "postedby")
	private String postedBy;

	@Column(name = "referenceid")
	private String referenceId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Recepient recepient;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "eventdetails", columnDefinition = "jsonb")
	private EventDetails eventDetails;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Action actions;

	@Column(name = "createdby")
	private String createdBy;

	@Column(name = "createdtime")
	private Long createdTime;

	@Column(name = "lastmodifiedby")
	private String lastModifiedBy;

	@Column(name = "lastmodifiedtime")
	private Long lastModifiedTime;
}
