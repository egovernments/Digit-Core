package org.egov.common.utils;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;

import static org.egov.common.constants.ServiceCommonConstants.*;

public class ResponseInfoUtil {

    public static ResponseInfo createResponseInfoFromRequestInfo(final RequestInfo requestInfo, final Boolean success) {

        final String apiId = requestInfo != null ? requestInfo.getApiId() : EMPTY_STRING;
        final String ver = requestInfo != null ? requestInfo.getVer() : EMPTY_STRING;
        Long ts = null;
        if(requestInfo != null)
            ts = requestInfo.getTs();
        final String resMsgId = RES_MESSAGE_ID;
        final String msgId = requestInfo != null ? requestInfo.getMsgId() : EMPTY_STRING;
        final String responseStatus = success ? SUCCESSFUL_STATUS : FAILED_STATUS;

        return ResponseInfo.builder().apiId(apiId).ver(ver).ts(ts).resMsgId(resMsgId).msgId(msgId).resMsgId(resMsgId)
                .status(responseStatus).build();
    }

}
