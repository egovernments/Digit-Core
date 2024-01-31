package org.egov.tracer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/*
* Note: This class needs to be removed !!!!!
* */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseInfo {

        private String apiId;
        private String ver;
        private Long ts;
        private String resMsgId;
        private String msgId;
        private String status;

}
