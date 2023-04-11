package org.egov.inbox.repository.builder.V2;


import org.egov.inbox.web.model.InboxRequest;
import org.egov.inbox.web.model.V2.InboxQueryConfiguration;

import java.util.Map;
import java.util.Optional;

public interface QueryBuilderInterface{

    /**
     * This method will take the incoming request and create
     * elasticsearch query based on input search criteria
     * @param inboxRequest
     * @return
     */
    public Map<String, Object> getESQuery(InboxRequest inboxRequest, Boolean isPaginationRequired);


    /**
     * This method will take the incoming request and create
     * elasticsearch query based on input search criteria
     * to get status count map
     * @param inboxRequest
     * @return
     */
    public Map<String, Object> getStatusCountQuery(InboxRequest inboxRequest);

    /**
     * This method will take the incoming request and create
     * elasticsearch query based on input search criteria
     * to get nearing sla count
     * @param inboxRequest
     * @return
     */
    public Map<String, Object> getNearingSlaCountQuery(InboxRequest inboxRequest, Long businessServiceSla);

}