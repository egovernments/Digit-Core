package com.example.demo.listner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;

//@Component
public class DataConsumer {

    @Autowired
    private ObjectMapper mapper;

    @KafkaListener(topics = {"${kafka.data.topic}"})
    public void processData(String record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic){
        /*try{
            System.out.println(mapper.writeValueAsString(record));
        }
        catch (Exception e){
            e.printStackTrace();
        }*/

        throw new RuntimeException("Invalid message");
    }


}
