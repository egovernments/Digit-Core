package org.egov.sunbirdrc.models;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrTemplateClient {
    private String schemaId;
    private String schemaVersion;
    private String template;
    private String type;
}
