
package org.egov.user.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class UserConfig {

    @Value("${kafka.topics.user.status.change}")
    private String userStatusChangeTopic;

    @Value("${user.status.change.event.enabled}")
    private boolean userStatusChangeEventEnabled;

}