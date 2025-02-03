package org.egov.infra.mdms.model;

import lombok.*;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MdmsTenantMasterCriteria {

    private String tenantId;

    private String schemaCode;

}
