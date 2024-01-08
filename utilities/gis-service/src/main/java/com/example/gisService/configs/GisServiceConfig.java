package com.example.gisService.configs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GisServiceConfig {

    @Value("${boundary.service.host}")
    private String boundaryServiceHost;

    @Value("${boundary.service.path}")
    private String boundaryServicePath;

}
