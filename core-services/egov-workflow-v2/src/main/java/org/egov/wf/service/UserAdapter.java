package org.egov.wf.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.wf.web.models.user.UserSearchRequest;

import java.util.List;
import java.util.Map;

public interface UserAdapter {

	Map<String, User> searchUser(RequestInfo requestInfo, List<String> uuids);

	List<String> searchUserUuidsBasedOnRoleCodes(UserSearchRequest userSearchRequest);

}
