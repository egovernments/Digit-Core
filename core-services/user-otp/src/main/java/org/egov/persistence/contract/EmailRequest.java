package org.egov.persistence.contract;

import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode
public class EmailRequest {
    private RequestInfo requestInfo;
    
    private Email email;
}
