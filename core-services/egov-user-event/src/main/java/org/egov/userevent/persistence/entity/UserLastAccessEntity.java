package org.egov.userevent.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Maps eg_usrevents_user_lat (last-access time per user, drives read/unread). */
@Entity
@Table(name = "eg_usrevents_user_lat")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLastAccessEntity {

	@Id
	@Column(name = "userid")
	private String userId;

	@Column(name = "lastaccesstime")
	private Long lastAccessTime;
}
