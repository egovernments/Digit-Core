package org.egov.infra.mdms.utils;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.egov.infra.mdms.utils.MDMSConstants.*;

public class QueryUtil {

    private QueryUtil(){}

    private static final Gson gson = new Gson();

    /**
     * This method aids in adding "WHERE" clause and "AND" condition depending on preparedStatementList i.e.,
     * if preparedStatementList is empty, it will understand that it is the first clause being added so it
     * will add "WHERE" to the query and otherwise it will
     * @param query
     * @param preparedStmtList
     */
    public static void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList){
        if(preparedStmtList.isEmpty()){
            query.append(" WHERE ");
        }else{
            query.append(" AND ");
        }
    }

    /**
     * This method returns a string with placeholders equal to the number of values that need to be put inside
     * "IN" clause
     * @param size
     * @return
     */
    public static String createQuery(Integer size) {
        StringBuilder builder = new StringBuilder();

        IntStream.range(0, size).forEach(i -> {
            builder.append(" ?");
            if (i != size - 1)
                builder.append(",");
        });

        return builder.toString();
    }

    /**
     * This method adds a set of String values into preparedStatementList
     * @param preparedStmtList
     * @param ids
     */
    public static void addToPreparedStatement(List<Object> preparedStmtList, Set<String> ids) {
        ids.forEach(id -> {
            preparedStmtList.add(id);
        });
    }

    /**
     * This method appends order by clause to the query
     * @param query
     * @param orderByClause
     * @return
     */
    public static String addOrderByClause(String query, String orderByClause){
        return query + orderByClause;
    }

    /**
     * This method prepares partial json string from the filter map to query on jsonb column
     * @param filterMap
     * @return
     */
    public static String preparePartialJsonStringFromFilterMap(Map<String, String> filterMap) {
        Map<String, Object> queryMap = new HashMap<>();

        filterMap.keySet().forEach(key -> {
            if(key.contains(DOT_SEPARATOR)){
                String[] keyArray = key.split(DOT_REGEX);
                Map<String, Object> nestedQueryMap = new HashMap<>();
                prepareNestedQueryMap(0, keyArray, nestedQueryMap, filterMap.get(key));
                queryMap.put(keyArray[0], nestedQueryMap.get(keyArray[0]));
            } else{
                queryMap.put(key, filterMap.get(key));
            }
        });

        String partialJsonQueryString = gson.toJson(queryMap);

        return partialJsonQueryString;
    }

    /**
     * Tail recursive method to prepare n-level nested partial json for queries on nested data in
     * master data. For e.g. , if the key is in the format a.b.c, it will construct a nested json
     * object of the form - {"a":{"b":{"c": "value"}}}
     * @param index
     * @param nestedKeyArray
     * @param currentQueryMap
     * @param value
     */
    private static void prepareNestedQueryMap(int index, String[] nestedKeyArray, Map<String, Object> currentQueryMap, String value) {
        // Return when all levels have been reached.
        if(index == nestedKeyArray.length)
            return;

        // For the final level simply put the value in the map.
        else if (index == nestedKeyArray.length - 1) {
            currentQueryMap.put(nestedKeyArray[index], value);
            return;
        }

        // For non terminal levels, add a child map.
        currentQueryMap.put(nestedKeyArray[index], new HashMap<>());

        // Make a recursive call to enrich data in next level.
        prepareNestedQueryMap(index + 1, nestedKeyArray, (Map<String, Object>) currentQueryMap.get(nestedKeyArray[index]), value);
    }

}
