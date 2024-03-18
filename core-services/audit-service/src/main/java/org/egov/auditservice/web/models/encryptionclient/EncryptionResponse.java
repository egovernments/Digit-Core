package org.egov.auditservice.web.models.encryptionclient;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EncryptionResponse {

    private List<Map<String, Object>> encResponseList = null;
}
