package com.tarento.analytics.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tarento.analytics.dto.AggregateDto;
import com.tarento.analytics.dto.AggregateRequestDto;
import com.tarento.analytics.dto.Data;
import com.tarento.analytics.dto.Plot;
import com.tarento.analytics.helper.ComputedFieldFactory;
import com.tarento.analytics.helper.IComputedField;
import com.tarento.analytics.model.ComputedFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CompareTableChartResponseHandler implements IResponseHandler{

    public static final Logger logger = LoggerFactory.getLogger(AdvanceTableChartResponseHandler.class);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ComputedFieldFactory computedFieldFactory;

    @Override
    public AggregateDto translate(AggregateRequestDto requestDto, ObjectNode aggregations) throws IOException {
        JsonNode aggregationNode = aggregations.get(AGGREGATIONS);
        JsonNode chartNode = requestDto.getChartNode();
        String plotLabel = chartNode.get(PLOT_LABEL).asText();
        JsonNode computedFields = chartNode.get(COMPUTED_FIELDS);
        JsonNode excludedFields = chartNode.get(EXCLUDED_COLUMNS);

        boolean executeComputedFields = computedFields !=null && computedFields.isArray();
        List<JsonNode> aggrNodes = aggregationNode.findValues(BUCKETS);
        boolean isPathSpecified = chartNode.get(IResponseHandler.AGGS_PATH)!=null && chartNode.get(IResponseHandler.AGGS_PATH).isArray();
        ArrayNode aggrsPaths = isPathSpecified ? (ArrayNode) chartNode.get(IResponseHandler.AGGS_PATH) : JsonNodeFactory.instance.arrayNode();

        int[] idx = { 1 };
        List<Data> dataList = new ArrayList<>();
        Map<String, Map<String, ObjectNode>> mappings = new HashMap<>();

        aggrNodes.stream().forEach(node -> {
            ArrayNode buckets = (ArrayNode) node;
            buckets.forEach(bucket -> {

                Map<String,ObjectNode> plotMap = new LinkedHashMap<>();
                String key = bucket.get(IResponseHandler.KEY).asText();

                if(aggrsPaths.size()>0){
                    processWithSpecifiedKeys(aggrsPaths, bucket, mappings, key, plotMap,chartNode);
                }
                if (plotMap.size() > 0) {
                    Map<String, ObjectNode> plots = new LinkedHashMap<>();
                    ObjectNode sno = mapper.createObjectNode();
                    sno.put(NAME,SERIAL_NUMBER);
                    sno.put(SYMBOL,TABLE_TEXT);
                    sno.put(LABEL, ""+idx[0]++);

                    ObjectNode plotkey = mapper.createObjectNode();
                    plotkey.put(NAME,plotLabel.isEmpty() ? TABLE_KEY : plotLabel);
                    plotkey.put(SYMBOL,TABLE_TEXT);
                    plotkey.put(LABEL, key);

                    plots.put(SERIAL_NUMBER, sno);
                    plots.put(plotLabel.isEmpty() ? TABLE_KEY : plotLabel, plotkey);
                    plots.putAll(plotMap);
                    mappings.put(key, plots);

                }
            });
        });

        mappings.entrySet().stream().forEach(plotMap -> {
            List<ObjectNode> plotList = plotMap.getValue().values().stream().collect(Collectors.toList());

            List<ObjectNode> filterPlot = plotList.stream().filter(c -> (!c.get("name").asText().equalsIgnoreCase(SERIAL_NUMBER) && !c.get("name").asText().equalsIgnoreCase(plotLabel))).collect(Collectors.toList());

            if(filterPlot.size()>0){
                Data data = new Data(plotMap.getKey(), Integer.parseInt(plotMap.getValue().get(SERIAL_NUMBER).get(LABEL).asText()), null);
                ObjectNode plotNode = mapper.convertValue(plotMap.getValue(),ObjectNode.class);
                if(executeComputedFields){
                    try {
                        List<ComputedFields> computedFieldsList = mapper.readValue(computedFields.toString(), new TypeReference<List<ComputedFields>>(){});
                        computedFieldsList.forEach(cfs -> {
                            IComputedField computedFieldObject = computedFieldFactory.getInstance(cfs.getActionName());
                            computedFieldObject.set(requestDto, cfs.getPostAggregationTheory());
                            computedFieldObject.add(plotNode, cfs.getFields(), cfs.getNewField(), chartNode );

                        });
                        List<Plot> plots = new ArrayList<>();
                        plotNode.forEach(node -> {
                            if(node.has(LABEL)){
                                Plot plot = new Plot(node.get(NAME).asText(), node.get(SYMBOL).asText());
                                plot.setLabel(node.get(LABEL).asText());
                                plots.add(plot);
                            }else if(node.has(VALUE)){
                                if(node.get(VALUE).getNodeType() == JsonNodeType.ARRAY){
                                    Plot plot = new Plot(node.get(NAME).asText(), (double) node.get(VALUE).size(), node.get(SYMBOL).asText());
                                    plots.add(plot);
                                }else {
                                    Plot plot = new Plot(node.get(NAME).asText(), node.get(VALUE).asDouble(), node.get(SYMBOL).asText());
                                    plots.add(plot);
                                }
                            }
                        });
                        if(excludedFields!=null){
                            List<String> list = mapper.readValue(excludedFields.toString(), new TypeReference<List<String>>(){});
                            List<Plot> removeplots = plots.stream().filter(c -> list.contains(c.getName())).collect(Collectors.toList());
                            plots.removeAll(removeplots);
                        }
                        data.setPlots(plots);
                    } catch (Exception e){
                        logger.error("execution of computed field :"+e.getMessage());
                    }
                }
                dataList.add(data);
            }
        });

        return getAggregatedDto(chartNode, dataList, requestDto.getVisualizationCode());
    }

    private void processWithSpecifiedKeys(ArrayNode aggrsPaths, JsonNode bucket,  Map<String, Map<String, ObjectNode>> mappings, String key, Map<String, ObjectNode> plotMap,JsonNode chartNode ){

        aggrsPaths.forEach(headerPath -> {
            JsonNode valueNode = bucket.findValue(headerPath.asText());
            ObjectNode plotNode = mapper.createObjectNode();
            plotNode.put(NAME,headerPath.asText());
            if(valueNode!=null){
                if (valueNode.has(BUCKETS)){
                    ArrayList keyList = (ArrayList) valueNode.findValuesAsText("key");
                    plotNode.putPOJO(VALUE,keyList);
                }else {
                    plotNode.put(VALUE, ( null == valueNode.findValue(VALUE) ? 0.0 : valueNode.findValue(VALUE).asDouble()));
                }
            }else{
                plotNode.put(VALUE,0.0);
            }
            String dataType = getDataType(chartNode, headerPath.asText(), valueNode);
            plotNode.put(SYMBOL,dataType);

            if(chartNode.get(IS_ROUND_OFF)!=null && chartNode.get(IS_ROUND_OFF).asBoolean() && plotNode.get(VALUE).isDouble()) {
                Double value =  (double) Math.round(plotNode.get(VALUE).asDouble());
                plotNode.put(VALUE,value);
            }

            if(mappings.containsKey(key)){
                if(mappings.get(key).get(headerPath.asText()) == null){
                    mappings.get(key).put(headerPath.asText(), plotNode);
                }else{
                    if(mappings.get(key).get(headerPath.asText()).get(VALUE).isDouble() ){
                        if(plotNode.get(VALUE).isDouble()){
                            double newVal = mappings.get(key).get(headerPath.asText()).get(VALUE).asDouble() + plotNode.get(VALUE).asDouble();
                            plotNode.put(VALUE,newVal);
                        }
                        mappings.get(key).put(headerPath.asText(), plotNode);
                    }
                }
            }else{
                plotMap.put(headerPath.asText(), plotNode);
            }

        });
    }

    private String getDataType(JsonNode chartNode, String headerName, JsonNode valueNode) {
        // TODO Auto-generated method stub
        if (chartNode.get("pathDataTypeMapping") != null) {
            JsonNode pathDataMapping = chartNode.get("pathDataTypeMapping");
            JsonNode node = pathDataMapping.findValue(headerName);
            return node.textValue();
        } else if( chartNode.get(VALUE_TYPE) != null) {
            return chartNode.get(VALUE_TYPE).asText();
        }else {
            return valueNode.isDouble() ? "amount" : "number";
        }
    }
}
