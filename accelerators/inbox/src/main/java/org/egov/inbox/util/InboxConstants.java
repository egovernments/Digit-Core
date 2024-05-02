package org.egov.inbox.util;

import org.springframework.stereotype.Component;

@Component
public class InboxConstants {

    public static final String INBOX_QUERY_CONFIG_NAME = "InboxQueryConfiguration";

    public static final String INBOX_MODULE_CODE = "INBOX";

    public static final String MDMS_RESPONSE_JSONPATH = "$.MdmsRes.INBOX.InboxQueryConfiguration[?(@.module==\'{{MODULE_NAME}}\')]";

    public static final String MODULE_PLACEHOLDER = "{{MODULE_NAME}}";

    public static final String SEARCH_PATH = "/_search";

    public static final String COUNT_PATH = "/_count";

    public static final String SORT_ORDER_CONSTANT = "sortOrder";

    public static final String SORT_BY_CONSTANT = "sortBy";

    public static final String CURRENT_PROCESS_INSTANCE_CONSTANT = "currentProcessInstance";

    public static final String COUNT_CONSTANT = "count";

    public static final String AUDIT_DETAILS_KEY = "auditDetails";

    public static final String CREATED_TIME_KEY = "createdTime";

    public static final String LAST_MODIFIED_TIME_KEY = "lastModifiedTime";

    public static final String BUSINESS_SERVICE_PATH = "$.currentProcessInstance.businessService";

    public static final String STATE_UUID_PATH = "$.currentProcessInstance.state.uuid";

    public static final String APPLICATION_STATUS_KEY = "applicationstatus";

    public static final String BUSINESSSERVICE_KEY = "businessservice";

    public static final String STATUSID_KEY = "statusid";

    public static final String AGGREGATIONS_KEY = "aggregations";

    public static final String STATUS_COUNT_AGGREGATIONS_BUCKETS_PATH = "$.aggregations.statusCount.buckets.*";

    public static final String KEY = "key";

    public static final String DOC_COUNT_KEY = "doc_count";

    public static final String HITS = "hits";

    public static final String SOURCE_KEY = "_source";

    public static final String DATA_KEY = "Data";

    public static final String SERVICESLA_KEY = "serviceSla";

    public static final String QUERY_KEY = "query";

    public static final String BOOL_KEY = "bool";

    public static final String MUST_KEY = "must";

    public static final String ORDER_KEY = "order";

    public static final String SORT_KEY = "sort";

    public static final String TENANTID_KEY = "tenantId";

    public static final String STATE = "state";
}
