package digit.web.models.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Email {

    private Set<String> emailTo;

    private String subject;

    private String body;

    Map<String, String> fileStoreId;

    private String tenantId;

    @JsonProperty("isHTML")
    private boolean isHTML;

}
