package org.egov.infra.mdms.repository.querybuilder;

import com.google.gson.Gson;
import org.egov.infra.mdms.model.MdmsCriteria;
import org.egov.infra.mdms.utils.QueryUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.egov.common.utils.MultiStateInstanceUtil.SCHEMA_REPLACE_STRING;
import static org.egov.infra.mdms.utils.MDMSConstants.FORM_CONFIG_SCHEMA_CODE;
import static org.egov.infra.mdms.utils.MDMSConstants.PROJECT_KEY;

@Component
public class FormConfigMdmsDataQueryBuilder {

    private static final Gson GSON = new Gson();

    private static final Pattern CONDITION_PATTERN = Pattern.compile("@\\.(\\w+)==(.+)");

    private static String SEARCH_MDMS_DATA_QUERY = "SELECT data.tenantid, data.uniqueidentifier, data.schemacode, data.data, data.isactive, data.createdby, data.lastmodifiedby, data.createdtime, data.lastmodifiedtime" +
            " FROM " + SCHEMA_REPLACE_STRING + ".eg_mdms_data data ";

    private static final String MDMS_DATA_QUERY_ORDER_BY_CLAUSE = " order by data.createdtime desc ";

    public String getMdmsDataSearchQuery(MdmsCriteria mdmsCriteria, List<Object> preparedStmtList) {
        String query = buildQuery(mdmsCriteria, preparedStmtList);
        query = QueryUtil.addOrderByClause(query, MDMS_DATA_QUERY_ORDER_BY_CLAUSE);
        return query;
    }

    private String buildQuery(MdmsCriteria mdmsCriteria, List<Object> preparedStmtList) {
        StringBuilder builder = new StringBuilder(SEARCH_MDMS_DATA_QUERY);
        Map<String, String> schemaCodeFilterMap = mdmsCriteria.getSchemaCodeFilterMap();

        if (!Objects.isNull(mdmsCriteria.getTenantId())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.tenantid LIKE ? ");
            preparedStmtList.add(mdmsCriteria.getTenantId() + "%");
        }
        if (!Objects.isNull(mdmsCriteria.getIds())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.id IN ( ").append(QueryUtil.createQuery(mdmsCriteria.getIds().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, mdmsCriteria.getIds());
        }
        if (!Objects.isNull(mdmsCriteria.getUniqueIdentifier())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.uniqueidentifier = ? ");
            preparedStmtList.add(mdmsCriteria.getUniqueIdentifier());
        }
        if (!CollectionUtils.isEmpty(schemaCodeFilterMap)) {
            String filterExpression = schemaCodeFilterMap.get(FORM_CONFIG_SCHEMA_CODE);
            if (filterExpression != null && !filterExpression.trim().isEmpty()) {
                // Push JSONB filter to DB; the OR guard ensures other schema rows are not excluded
                QueryUtil.addClauseIfRequired(builder, preparedStmtList);
                builder.append(" (data.schemacode != '").append(FORM_CONFIG_SCHEMA_CODE)
                        .append("' OR data.data @> CAST( ? AS jsonb ))");
                preparedStmtList.add(parseJsonPathToJsonbString(filterExpression));
            }
        }
        if (!Objects.isNull(schemaCodeFilterMap)) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.schemacode IN ( ").append(QueryUtil.createQuery(schemaCodeFilterMap.keySet().size())).append(" )");
            QueryUtil.addToPreparedStatement(preparedStmtList, schemaCodeFilterMap.keySet());
        }
        if (!Objects.isNull(mdmsCriteria.getIsActive())) {
            QueryUtil.addClauseIfRequired(builder, preparedStmtList);
            builder.append(" data.isactive = ? ");
            preparedStmtList.add(mdmsCriteria.getIsActive());
        }
        return builder.toString();
    }

    /**
     * Converts a JSONPath filter expression like
     * {@code [?(@.project=='CMP-123' && @.isSelected==true)]}
     * into a JSONB containment JSON string like
     * {@code {"project":"CMP-123","isSelected":true}}.
     */
    static String parseJsonPathToJsonbString(String filterExpression) {
        return GSON.toJson(parseConditions(filterExpression));
    }

    /**
     * Parses a JSONPath filter expression like
     * {@code [?(@.project=='CMP-123' && @.isSelected==true)]} into a map of its
     * equality conditions, e.g. {@code {"project":"CMP-123","isSelected":true}}.
     */
    static Map<String, Object> parseConditions(String filterExpression) {
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        if (filterExpression == null)
            return jsonMap;

        String stripped = filterExpression.trim();
        if (stripped.startsWith("[?(") && stripped.endsWith(")]")) {
            stripped = stripped.substring(3, stripped.length() - 2);
        }

        for (String condition : stripped.split("&&")) {
            Matcher matcher = CONDITION_PATTERN.matcher(condition.trim());
            if (matcher.find()) {
                String field = matcher.group(1);
                String raw = matcher.group(2).trim();
                jsonMap.put(field, parseValue(raw));
            }
        }
        return jsonMap;
    }

    /**
     * Extracts the value of the {@code project} condition from a FormConfig JSONPath
     * filter expression, or {@code null} if no project condition is present.
     */
    public static String extractProject(String filterExpression) {
        Object project = parseConditions(filterExpression).get(PROJECT_KEY);
        return project == null ? null : project.toString();
    }

    private static Object parseValue(String raw) {
        if (raw.startsWith("'") && raw.endsWith("'")) {
            return raw.substring(1, raw.length() - 1);
        }
        if ("true".equalsIgnoreCase(raw)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(raw)) return Boolean.FALSE;
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return raw;
        }
    }
}
