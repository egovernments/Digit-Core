package com.example.gateway.filters.post;

import com.example.gateway.model.PostHookFilterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.web.exchanges.HttpExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import com.example.gateway.utils.UrlProvider;
import com.example.gateway.utils.ExceptionUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
@Component
public class PostHookFilter implements GlobalFilter , Ordered {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${custom.filter.posthooks:false}")
    private boolean loadPostHookFilters;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Implement PostHook
        String uri = exchange.getRequest().getURI().toString();
        if(loadPostHookFilters && UrlProvider.getUrlPostHooksMap().get(uri) != null) {
            String resBody = readResponseBody(exchange);
            if (StringUtils.isEmpty(resBody)) return null;

            DocumentContext resDc = JsonPath.parse(resBody);
            DocumentContext reqDc = parseRequest(exchange);
            PostHookFilterRequest req = PostHookFilterRequest.builder().Request(reqDc.jsonString()).Response(resDc.jsonString()).build();
            String responseBody = null;
            try {
                log.debug("Executing post-hook filter. Sending request to - " + UrlProvider.getUrlPostHooksMap().get(uri));
                responseBody = restTemplate.postForObject(UrlProvider.getUrlPostHooksMap().get(uri), req,
                        String.class);
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                log.error("POST-Hook - Http Exception Occurred", e);
                ExceptionUtils.raiseCustomException(e.getStatusCode(), "POST_HOOK_ERROR - Post-hook url threw an error - " + e.getMessage().toString());
            } catch (Exception e) {
                log.error("POST-Hook - Exception Occurred", e);
                ExceptionUtils.raiseCustomException(HttpStatus.BAD_REQUEST, "POST_HOOK_ERROR - Post-hook url threw an error - " + e.getMessage());
            }

            ServerHttpResponse response = exchange.getResponse();

            // Modify the response body
            byte[] responseBytes = responseBody.getBytes();
            DataBuffer buffer = response.bufferFactory().wrap(responseBytes);
            response.getHeaders().setContentLength(responseBody.length());
            response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
            response.writeWith(Mono.just(buffer));
            return chain.filter(exchange);
        }
        return chain.filter(exchange);
    }

    private String readResponseBody(ServerWebExchange exchange) {
        String responseBody = null;
        try (final InputStream responseDataStream = objectMapper.convertValue(exchange.getResponse(),InputStream.class)) {
            responseBody = CharStreams.toString(new InputStreamReader(responseDataStream, "UTF-8"));
            //ctx.setResponseBody(responseBody);
        } catch (IOException e) {
            log.error("Error reading body", e);
        } catch (Exception e) {
            log.error("Exception while reading response body: " + e.getMessage());
        }
        return responseBody;
    }

    private DocumentContext parseRequest(ServerWebExchange exchange) {

        String payload = null;
        try {
            InputStream is = objectMapper.convertValue(exchange.getRequest(), InputStream.class);
            payload = IOUtils.toString(is);
            //request.getRequestURI();
        } catch (IOException e) {
            throw new CustomException("REQUEST_PARSING_ERROR", e.getMessage());
        }
        return JsonPath.parse(payload);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE-3;
    }
}
