package org.egov.filters.pre;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.egov.Utils.Utils;
import org.egov.model.HttpRequestLog;
import org.egov.producer.Producer;
import org.egov.wrapper.CustomRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import static org.egov.constants.RequestContextConstants.CORRELATION_ID_KEY;

@Component
public class RequestLogFilter extends ZuulFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${log.requests.to.es:false}")
    private Boolean logRequestsToEs;

    @Autowired
    private Producer producer;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 6;
    }

    @Override
    public boolean shouldFilter() {
        return logRequestsToEs;
    }

    @Override
    public Object run() {
        try {
            HttpRequestLog requestLog = createRequestLogFromRequestContext();

            if(!ObjectUtils.isEmpty(requestLog))
                producer.push("process-correlator-requests-kafka-topic", requestLog);
            else
                logger.error("An exception occurred, request log won't be pushed to ES");

        } catch(Exception ex) {
            logger.error("An exception occurred, request log won't be pushed to ES - ", ex);
        }
        return null;
    }

    /**
     * This method creates request log to be persisted on ElasticSearch to log incoming request.
     * @return
     * @throws IOException
     */
    private HttpRequestLog createRequestLogFromRequestContext() throws IOException {
        // Get current request context
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest httpServletRequest = ctx.getRequest();

        // Check if the incoming request is of the type application/json
        if (!Utils.isRequestBodyCompatible(httpServletRequest))
            return null;

        // Initialize custom request wrapper with the received servlet request and extract request body and request headers
        CustomRequestWrapper requestWrapper = new CustomRequestWrapper(httpServletRequest);
        HashMap<String, Object> requestBody = getRequestBody(requestWrapper);
        HashMap<String, Object> requestHeaders = getRequestHeadersFromContext(httpServletRequest);

        // Build request log to be persisted on ES
        HttpRequestLog requestLog = HttpRequestLog.builder()
            .method(httpServletRequest.getMethod())
            .body(mapper.writeValueAsString(requestBody))
            .createdTime(System.currentTimeMillis())
            .url(httpServletRequest.getRequestURI())
            .queryParams(ctx.getRequestQueryParams())
            .headers(requestHeaders)
            .correlationId((String) ctx.get(CORRELATION_ID_KEY))
            .build();

        return requestLog;
    }

    /**
     * This method extracts out the headers from the incoming servlet request.
     * @param request
     * @return
     */
    private HashMap<String, Object> getRequestHeadersFromContext(HttpServletRequest request) {
        HashMap<String, Object> headerMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        // Traverse header names, get the value present against that header and put them in the headerMap
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headerMap.put(headerName, headerValue);
        }

        return headerMap;
    }

    /**
     * This method extracts request body from the incoming request.
     * @param requestWrapper
     * @return
     * @throws IOException
     */
    private HashMap<String, Object> getRequestBody(CustomRequestWrapper requestWrapper) throws IOException {
        return mapper.readValue(requestWrapper.getPayload(),
            new TypeReference<HashMap<String, Object>>() { });
    }

}
