package com.tarento.analytics.helper;

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
import static com.tarento.analytics.handler.IResponseHandler.*;

/**
 * Data is passed as objectNode and performs similar to AdditiveComputedField, doesn't compare indices
 */

@Component

public class CompareAdditiveComputedField implements IComputedField<ObjectNode>{
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
        Double Total = 0.0;
        newFieldNode.put(VALUE,Total);
        try{
            for (String field : fields){
                Total = Total + data.get(field).get(VALUE).asDouble();
            }
            newFieldNode.put(VALUE,Total);
            data.set(newField,newFieldNode);
        }catch (Exception e) {
            logger.error("could not be Added " +e.getMessage());
            data.set(newField, newFieldNode);
        }
    }
}