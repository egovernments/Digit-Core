package com.example.gateway.filters.pre;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
public class RequestBodyRewrite implements RewriteFunction<Map, Map> {

    @Value("${egov.auth-service-host}")
    private  String authServiceHost;

    @Value("${egov.auth-service-uri}")
    private  String authUri;

    @Autowired
    private Gson gson;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Publisher<Map> apply(ServerWebExchange serverWebExchange, Map body) {
        try {
            RequestInfo requestInfo = objectMapper.convertValue(body.get("RequestInfo"), RequestInfo.class);
            requestInfo.setUserInfo(getUser(requestInfo.getAuthToken()));
            body.put("RequestInfo", requestInfo);
            return Mono.just(body);
        } catch (Exception ex) {
            log.error("An error occured while transforming the request body in class RequestBodyRewrite. {}", ex);

            // Throw custom exception here
            throw new RuntimeException(
                    "An error occured while transforming the request body in class RequestBodyRewrite.");

        }
    }

    private User getUser(String authToken) {
        String authURL = String.format("%s%s%s", authServiceHost, authUri, authToken);

        User user;

        try {
            user = restTemplate.postForObject(authURL, null, User.class);
        } catch (Exception e) {
            throw new CustomException("Exception occurred while fetching user: ", e.getMessage());
        }

        return user;
    }
}
