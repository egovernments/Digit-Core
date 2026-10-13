package org.digit.services.registry.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistryCacheEntry implements Serializable {
    private String registryId;
    private Integer version;
}
