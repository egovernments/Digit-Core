package com.example.gateway.utils;

import com.example.gateway.model.EventLogRequest;
import com.example.gateway.model.RequestCaptureCriteria;
import com.example.gateway.producer.Producer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

@Component
@Slf4j
public class EventLoggerUtil {

    @Autowired
    Producer producer;

    @Value("${eventlog.captureInputBody:false}")
    private boolean captureInputBody;

    @Value("${eventlog.captureOutputBody:false}")
    private boolean captureOutputBody;

    @Value("${eventlog.captureOutputBodyOnlyOnError:true}")
    private boolean captureOutputBodyOnlyOnError;

    private RequestCaptureCriteria criteria;

    @PostConstruct
    public void init(){
        criteria = RequestCaptureCriteria.builder()
                .captureInputBody(captureInputBody)
                .captureOutputBody(captureOutputBody)
                .captureOutputBodyOnlyForError(captureOutputBodyOnlyOnError)
                .build();
    }

    public Object logCurrentRequest(ServerWebExchange exchange , Map body, String topic){
        try {
            EventLogRequest request = EventLogRequest.fromRequestContext(exchange , body , criteria);
            producer.push(topic, request);
        } catch (Exception ex) {
            log.error("event logger", ex);
        }
        return null;
    }
}
