package com.example.gateway.model;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Role {

	private Long id;

	@Size(max = 128)
	private String name;

	@Size(max = 128)
	private String description;

	@Size(max = 50)
	private String code;
	private Date createdDate;
	private Long createdBy;
	private Date lastModifiedDate;
	private Long lastModifiedBy;
}
