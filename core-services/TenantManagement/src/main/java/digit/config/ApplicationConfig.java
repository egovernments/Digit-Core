package digit.config;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ApplicationConfig {

    @Value("${kafka.topics.create.tenant}")
    private String createTopic;

    @Value("${kafka.topics.create.tenant.config}")
    private String createConfigTopic;

    @Value("${kafka.topics.update.tenant}")
    private String updateTopic;

    @Value("${mdms.default.offset}")
    private Integer defaultOffset;

    @Value("${mdms.default.limit}")
    private Integer defaultLimit;

    @Value("${egov.user.host}${egov.user.search.path}")
    private String UserSearchURI;

    @Value("${kafka.topics.notification.mail.name}")
    private String emailTopic;
}
