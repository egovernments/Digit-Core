package org.egov.persistence.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
public class Email {

	private Set<String> emailTo;
	private String subject;
	private String body;

}
