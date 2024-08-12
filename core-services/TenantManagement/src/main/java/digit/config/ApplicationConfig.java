package digit.config;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ToString
@Setter
@Getter
//@Import({MultiStateInstanceUtil.class})
public class ApplicationConfig {

//    @Value("${mdms.default.offset}")
    private Integer defaultOffset;

//    @Value("${mdms.default.limit}")
    private Integer defaultLimit;
}
