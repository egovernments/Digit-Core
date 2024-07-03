package digit.web.models;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MdmsRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("Mdms")
    @Valid
    private Mdms mdms = null;

}
