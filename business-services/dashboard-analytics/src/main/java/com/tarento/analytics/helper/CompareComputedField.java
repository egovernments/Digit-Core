package com.tarento.analytics.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tarento.analytics.dto.AggregateRequestDto;
import com.tarento.analytics.handler.IResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
@Component
public class CompareComputedField implements IComputedField<ObjectNode>{

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
        newFieldNode.put(IResponseHandler.NAME,newField);
        newFieldNode.set(IResponseHandler.SYMBOL,data.get(fields.get(0)).get(IResponseHandler.SYMBOL));
        Double count = 0.0;
        newFieldNode.put(IResponseHandler.VALUE,count);
        try {
            if(fields.size() == 2 && data.get(fields.get(0)) != null && data.get(fields.get(1)) != null){
                if(data.get(fields.get(0)).get(IResponseHandler.VALUE).isArray()
                        &&  data.get(fields.get(1)).get(IResponseHandler.VALUE).isArray()){
                    List<String> listA = mapper.convertValue(data.get(fields.get(0)).get(IResponseHandler.VALUE), new TypeReference<List>() {});
                    List<String> listB = mapper.convertValue(data.get(fields.get(1)).get(IResponseHandler.VALUE), new TypeReference<List>() {});
                    for (String str : listA) {
                        if (!listB.contains(str)) {
                            count++;
                        }
                    }

                }else {
                    count = data.get(fields.get(0)).get(IResponseHandler.VALUE).isArray() ? (double) data.get(fields.get(0)).get(IResponseHandler.VALUE).size()
                                : data.get(fields.get(0)).get(IResponseHandler.VALUE).asDouble();
                }
                newFieldNode.put(IResponseHandler.VALUE,count);
                data.set(newField,newFieldNode);
            }
        }catch (Exception e) {
            logger.error("could not be compared " +e.getMessage());
            data.set(newField, newFieldNode);
        }

    }
}
