package org.egov.sunbirdrc.models;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MdmsData {
    private String uuid;
    private List<String> path;
    private String code;

}
