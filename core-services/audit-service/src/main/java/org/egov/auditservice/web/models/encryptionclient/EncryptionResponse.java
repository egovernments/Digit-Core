package org.egov.auditservice.web.models.encryptionclient;

import lombok.*;

import java.util.LinkedList;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EncryptionResponse {

    private LinkedList<Map<String, Object>> encResponseList = null;
}
