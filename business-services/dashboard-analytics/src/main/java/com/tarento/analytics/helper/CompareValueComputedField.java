package com.tarento.analytics.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tarento.analytics.dto.AggregateRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.tarento.analytics.handler.IResponseHandler.SYMBOL;
import static com.tarento.analytics.handler.IResponseHandler.VALUE;
import static com.tarento.analytics.handler.IResponseHandler.NAME;

@Component
public class CompareValueComputedField implements IComputedField<ObjectNode>{

    public static final Logger logger = LoggerFactory.getLogger(AdditiveComputedField.class);

    private String postAggrTheoryName;
    private AggregateRequestDto aggregateRequestDto;
    @Autowired
    private ComputeHelperFactory computeHelperFactory;
    @Autowired
    private ObjectMapper mapper;

    @Override
    public void set(AggregateRequestDto requestDto, String postAggrTheoryName) {
        this.aggregateRequestDto = requestDto;
        this.postAggrTheoryName = postAggrTheoryName;
    }

    @Override
    public void add(ObjectNode data, List<String> fields, String newField, JsonNode chartNode) {
        ObjectNode newFieldNode = JsonNodeFactory.instance.objectNode();
        newFieldNode.put(NAME,newField);
        newFieldNode.set(SYMBOL,data.get(fields.get(0)).get(SYMBOL));
        Double count = 0.0;
        newFieldNode.put(VALUE,count);
        try {
            if(fields.size() == 2 && data.get(fields.get(0)) != null && data.get(fields.get(1)) != null){
                if(data.get(fields.get(0)).get(VALUE).isObject()
                        &&  data.get(fields.get(1)).get(VALUE).isObject()){
                    Map<String,Double> nodeA = mapper.convertValue(data.get(fields.get(0)).get(VALUE), new TypeReference<Map<String, Double>>() {
                    });
                    JsonNode nodeB = data.get(fields.get(1)).get(VALUE);
                    final Double[] val = {0.0};
                    nodeA.forEach((key,value) -> {
                        val[0] += nodeB.has(key) ? (value - nodeB.get(key).asDouble()) : value;
                    });
                    count = val[0];
                }else {
                    final Double[] nodeAVal = {0.0};
                    data.get(fields.get(0)).get(VALUE).forEach(value-> {
                        nodeAVal[0]=nodeAVal[0]+value.asDouble();
                    });
                    count = data.get(fields.get(0)).get(VALUE).isObject() ? nodeAVal[0]
                            : data.get(fields.get(0)).get(VALUE).asDouble();
                }
            }
            newFieldNode.put(VALUE,count);
            data.set(newField,newFieldNode);
        }catch (Exception e) {
            logger.error("could not be compared " +e.getMessage());
            data.set(newField, newFieldNode);
        }
    }
}