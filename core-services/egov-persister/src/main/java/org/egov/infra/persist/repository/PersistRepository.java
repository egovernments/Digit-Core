package org.egov.infra.persist.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.egov.infra.persist.web.contract.JsonMap;
import org.egov.infra.persist.web.contract.TypeEnum;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

@Repository
@Slf4j
public class PersistRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Configuration LENIENT_VALUE_READ =
            Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

    private <T> T readValue(Object jsonObj, String jsonPath) {
        return JsonPath.using(LENIENT_VALUE_READ).parse(jsonObj).read(jsonPath);
    }


    public void persist(String query, List<Object[]> rows) {

        try {
            if( ! rows.isEmpty()) {
                log.info("Executing query : "+ query);
                int[] affected = jdbcTemplate.batchUpdate(query, rows);
                logAffectedRows(query, rows.size(), affected);
            }
        } catch (Exception ex) {
            log.error("Failed to persist {} row(s) using query: {}", rows.size(), query, ex);
            throw ex;
        }
    }

    public void persist(String query, List<JsonMap> jsonMaps, Object jsonObj, String baseJsonPath) {

        List<Object[]> rows = getRows(jsonMaps,jsonObj,baseJsonPath);

        try {
            if( ! rows.isEmpty()) {
                log.info("Executing query : "+ query);
                int[] affected = jdbcTemplate.batchUpdate(query, rows);
                logAffectedRows(query, rows.size(), affected);
            }
        } catch (Exception ex) {
            log.error("Failed to persist {} row(s) using query: {}", rows.size(), query, ex);
            throw ex;
        }
    }

    /**
     * How the counts the database reported compare with what was submitted.
     */
    enum PersistOutcome {
        /** Everything submitted changed a row (or more rows changed than submitted). */
        HEALTHY,
        /** The driver returned no affected-row counts, so nothing can be judged either way. */
        COUNTS_UNAVAILABLE,
        /** Rows were suppressed by an "ON CONFLICT ... DO NOTHING" - a by-design no-op, not a loss. */
        DUPLICATE_SUPPRESSED,
        /** Some submitted rows changed nothing, and the statement is not a by-design no-op. */
        PARTIAL_PERSIST,
        /** Nothing at all changed, and the statement is not a by-design no-op. */
        SILENT_WRITE_LOSS
    }

    /** Tally of one JDBC batch result. */
    record BatchTally(int changed, int unknown, int failed) {}

    /**
     * "INSERT ... ON CONFLICT ... DO NOTHING" (with or without a conflict target / ON CONSTRAINT
     * clause, across line breaks). Anchored on INSERT and kept inside one statement ([^;]) so a
     * mapping that ends in a plain UPDATE cannot be excused by a DO NOTHING elsewhere in the string.
     */
    private static final Pattern INSERT_CONFLICT_DO_NOTHING = Pattern.compile(
            "insert\\b[^;]*?\\bon\\s+conflict\\b[^;]*?\\bdo\\s+nothing\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * True when the statement is allowed to change fewer rows than were submitted.
     *
     * "INSERT ... ON CONFLICT ... DO NOTHING" is used to make a create mapping idempotent under
     * Kafka redelivery: the duplicate is suppressed and Postgres reports 0 affected rows. That is
     * the fix working, not a write loss, and reporting it as one would train operators to ignore
     * the very signal that makes real write loss visible. "DO UPDATE" is deliberately NOT included:
     * an upsert always reports one row per submitted row, so 0 there still means the statement
     * matched nothing and is still worth an ERROR.
     */
    static boolean suppressesDuplicates(String query) {
        return query != null && INSERT_CONFLICT_DO_NOTHING.matcher(query).find();
    }

    /** Count what the database reported. A null/empty result is tallied as all-zero. */
    static BatchTally tally(int[] affected) {
        int changed = 0;
        int unknown = 0;
        int failed = 0;
        if (affected != null) {
            for (int count : affected) {
                if (count == java.sql.Statement.SUCCESS_NO_INFO)
                    unknown++;
                else if (count == java.sql.Statement.EXECUTE_FAILED)
                    failed++;
                else
                    changed += count;
            }
        }
        return new BatchTally(changed, unknown, failed);
    }

    /**
     * Decide what a batchUpdate result means. Kept free of logging so it can be asserted directly.
     *
     * batchUpdate returns one count per submitted row. Discarding it makes an UPDATE whose WHERE
     * clause matched nothing indistinguishable from a successful write, which is how a mapping keyed
     * on a column that can legitimately be NULL becomes an undetectable write loss.
     */
    static PersistOutcome classify(String query, int submitted, int[] affected) {
        // No counts at all: JdbcTemplate sizes the result array to the batch, so this is only
        // reachable defensively (a null from a stub / a mock). Judge nothing.
        if (affected == null || affected.length == 0)
            return PersistOutcome.COUNTS_UNAVAILABLE;

        BatchTally tally = tally(affected);

        if (tally.unknown() > 0 && tally.changed() == 0)
            return PersistOutcome.COUNTS_UNAVAILABLE;
        if (tally.changed() >= submitted)
            return PersistOutcome.HEALTHY;
        // A failed statement is never excused by the mapping being idempotent.
        if (tally.failed() == 0 && suppressesDuplicates(query))
            return PersistOutcome.DUPLICATE_SUPPRESSED;
        return tally.changed() == 0 ? PersistOutcome.SILENT_WRITE_LOSS : PersistOutcome.PARTIAL_PERSIST;
    }

    /**
     * Report what the database actually changed, not how many parameter rows were submitted.
     *
     * The healthy case keeps the original "Persisted {} row(s) to DB!" wording so existing log
     * scraping and throughput measurement keep working.
     */
    private void logAffectedRows(String query, int submitted, int[] affected) {
        // Defensive: nothing was reported, so keep the original wording rather than invent an alarm.
        if (affected == null || affected.length == 0) {
            log.info("Persisted {} row(s) to DB!", submitted);
            return;
        }

        BatchTally tally = tally(affected);

        if (tally.failed() > 0)
            log.error("Persist reported {} failed statement(s) of {} submitted for query: {}",
                    tally.failed(), submitted, query);

        switch (classify(query, submitted, affected)) {
            case COUNTS_UNAVAILABLE -> log.info("Persisted {} row(s) to DB! (driver returned no " +
                    "affected-row counts, so whether rows changed is unknown)", submitted);
            case SILENT_WRITE_LOSS -> log.error("SILENT WRITE LOSS: {} row(s) submitted but the " +
                    "database changed 0 row(s). The statement matched nothing - check the WHERE " +
                    "clause against columns that can be NULL. Query: {}", submitted, query);
            case PARTIAL_PERSIST -> log.warn("PARTIAL PERSIST: {} row(s) submitted but the database " +
                    "changed only {} row(s). Query: {}", submitted, tally.changed(), query);
            case DUPLICATE_SUPPRESSED -> log.info("Persisted {} of {} row(s) to DB; {} row(s) " +
                    "suppressed by ON CONFLICT ... DO NOTHING (already present - idempotent replay, " +
                    "not a write loss). Query: {}",
                    tally.changed(), submitted, submitted - tally.changed(), query);
            default -> log.info("Persisted {} row(s) to DB!", tally.changed());
        }
    }


    public List<Object[]> getRows(List<JsonMap> jsonMaps, Object jsonObj, String baseJsonPath) {

        List<LinkedHashMap<String, Object>> dataSource = extractData(baseJsonPath, jsonObj);

        if (dataSource == null || dataSource.isEmpty()) {
            log.debug("No data found for basePath: {}", baseJsonPath);
            return new ArrayList<>();
        }

        List<Object[]> rows = new ArrayList<>();
        int nullRecords = 0;
        int emptyChildRecords = 0;

        for (int i = 0; i < dataSource.size(); i++) {
            LinkedHashMap<String, Object> rawDataRecord = dataSource.get(i);

            if (rawDataRecord == null) {
                nullRecords++;
                continue;
            }

            if (isChildObjectEmpty(baseJsonPath, rawDataRecord)) {
                emptyChildRecords++;
                continue;
            }


            List<Object> row = new ArrayList<>();
            for (JsonMap jsonMap : jsonMaps) {
                String jsonPath = jsonMap.getJsonPath();
                TypeEnum type = jsonMap.getType();
                TypeEnum dbType = jsonMap.getDbType();
                Object value = null;

//                if(isNull(jsonPath))
//                    throw new NullPointerException("JSON Path is null: "+jsonMap);


                if (type == null) {
                    type = TypeEnum.STRING;
                }

                if (jsonPath.contains("{")) {
                    String attribute = jsonPath.substring(jsonPath.indexOf("{") + 1, jsonPath.indexOf("}"));
                    jsonPath = jsonPath.replace("{".concat(attribute).concat("}"), "\"" + rawDataRecord.get(attribute).toString() + "\"");
                    JSONArray jsonArray = readValue(jsonObj, jsonPath);
                    row.add(jsonArray.get(0));

                    continue;

                }

                else if (type.equals(TypeEnum.CURRENTDATE)) {
                    if (dbType.equals(TypeEnum.DATE))
                        row.add(new Date());
                    else if (dbType.equals(TypeEnum.LONG))
                        row.add(new Date().getTime());
                    continue;
                }

                else if ((type.equals(TypeEnum.ARRAY)) && dbType.equals(TypeEnum.STRING)) {
                    List<Object> list1 = readValue(jsonObj, jsonPath);
                    if (CollectionUtils.isEmpty(list1)) {
                        value = null;
                    } else {
                        value = StringUtils.join(list1.get(i), ",");
                        value = value.toString().substring(2, value.toString().lastIndexOf("]") - 1).replace("\"", "");
                    }
                }

                else if (jsonPath.contains("*.")) {
                    jsonPath = jsonPath.substring(jsonPath.lastIndexOf("*.") + 2);
                    value = extractValueFromTree(rawDataRecord, jsonPath);
                }

                else if (!(type.equals(TypeEnum.CURRENTDATE) || jsonPath.startsWith("default"))) {
                    value = readValue(jsonObj, jsonPath);
                }

                if (jsonPath.startsWith("default"))
                    row.add(null);

                else if (type.equals(TypeEnum.JSON) && dbType.equals(TypeEnum.STRING)) {
                    try {
                        String json = objectMapper.writeValueAsString(value);
                        row.add(json);
                    } catch (JsonProcessingException e) {
                        log.error("Error while processing JSON object to string", e);
                    }
                }

                else if (type.equals(TypeEnum.JSON) && dbType.equals(TypeEnum.JSONB)) {
                    try {
                        String json = objectMapper.writeValueAsString(value);

                        PGobject pGobject = new PGobject();
                        pGobject.setType("jsonb");
                        pGobject.setValue(json);
                        row.add(pGobject);
                    } catch (JsonProcessingException e) {
                        log.error("Error while processing JSON object to string", e);
                    } catch (SQLException e) {
                        log.error("Error while setting JSONB object", e);
                    }
                }

                else if (type.equals(TypeEnum.LONG)) {
                    if (dbType == null)
                        row.add(value);
                    else if (dbType.equals(TypeEnum.DATE))
                        row.add(new java.sql.Date(Long.parseLong(value.toString())));
                }

                else if (type.equals(TypeEnum.DATE) & value != null) {

                    String date = value.toString();
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    java.util.Date startDate = null;
                    try {
                        startDate = df.parse(date);
                    } catch (ParseException e) {
                        log.error("Unable to parse date", e);
                    }
                    row.add(startDate);
                }

                else
                    row.add(value);

            }
            rows.add(row.toArray());
        }

        if (nullRecords > 0 || emptyChildRecords > 0) {
            log.debug("getRows for basePath '{}': {} rows extracted, {} null records skipped, {} empty child records skipped",
                    baseJsonPath, rows.size(), nullRecords, emptyChildRecords);
        }

        return rows;

    }

    /**
     * Extract data from the tree using provided base json path
     *  - If base path signifies bulk, then extract array of data
     *  - If base path is not bulk, then extract single row of data and wrap as list
     *
     * @param baseJsonPath Base json path
     * @param document Data source tree
     * @return Partial data source tree based on provided json base path
     */
    private List<LinkedHashMap<String, Object>> extractData(String baseJsonPath, Object document) {
        List<LinkedHashMap<String, Object>> list = null;
        if(baseJsonPath.contains("*")) {
            String arrayBasePath = baseJsonPath.substring(0, baseJsonPath.lastIndexOf(".*") + 2);
            list = JsonPath.read(document, arrayBasePath);
        }
        else {
            LinkedHashMap<String, Object> map = JsonPath.read(document, baseJsonPath);
            list = Collections.singletonList(map);
        }
        return list;
    }


    /**
     * Fetch leaf node value recursively based on json path from java represented json tree
     *
     * @param jsonTree Java represented json tree
     * @param jsonPath Path of leaf node
     * @return Value of leaf node
     */
    private Object extractValueFromTree(LinkedHashMap<String, Object> jsonTree, String jsonPath) {
        String[] objDepth = jsonPath.split("\\.");
        Object value = null;
        LinkedHashMap<String, Object> jsonTree1 = null;

        for (int k = 0; k < objDepth.length; k++) {

            if (objDepth.length > 1 && k != objDepth.length - 1) {
                if (jsonTree1 == null)
                    jsonTree1 = (LinkedHashMap<String, Object>) jsonTree.get(objDepth[k]);
                else
                    jsonTree1 = (LinkedHashMap<String, Object>) jsonTree1.get(objDepth[k]);
                if (jsonTree1 == null) {
                    value = null;
                    break;
                }

            }

            if (k == objDepth.length - 1) {
                if (jsonTree1 != null)
                    value = jsonTree1.get(objDepth[k]);
                else
                    value = jsonTree.get(objDepth[k]);
            }
        }
        return value;
    }

    /**
     * Check if leaf node, is null,
     *  for ex, user has optional address in config, if address is null in datasource skip persisting to address table
     *
     * @param baseJsonPath Base json path
     * @param jsonTree Java represented json tree
     * @return If node not available, return true, else false
     */
    private boolean isChildObjectEmpty(String baseJsonPath, LinkedHashMap<String, Object> jsonTree) {

        if ( baseJsonPath.contains("*") && ! baseJsonPath.endsWith("*")) {
            String baseJsonPathForNullCheck = baseJsonPath.substring(baseJsonPath.lastIndexOf("*.") + 2);
            String[] baseObjectsForNullCheck = baseJsonPathForNullCheck.split("\\.");
            LinkedHashMap<String, Object> temp = new LinkedHashMap<>(jsonTree);
            for (String baseObjectForNullCheck : baseObjectsForNullCheck) {
                if (isNull(temp.get(baseObjectForNullCheck))) {
                    log.info("Skipping persisting record with basePath {} as it's empty!", baseJsonPath);
                    return true;
                }
                else
                    temp = (LinkedHashMap<String, Object>) temp.get(baseObjectForNullCheck);
            }
            return false;
        } else
            return false;
    }


}