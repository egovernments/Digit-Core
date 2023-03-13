package org.egov.inbox.service.V2.validator;

import org.egov.inbox.web.model.InboxRequest;
import org.egov.tracer.model.CustomException;

public interface SearchCriteriaValidatorInterface {

    /**
     * This method will validate if the input search criteria is
     * valid or not for the given module. It will return true if
     * the criteria is valid else it will return false
     * @param inboxRequest
     * @return
     */
    public void validateSearchCriteria(InboxRequest inboxRequest) throws CustomException;

}
