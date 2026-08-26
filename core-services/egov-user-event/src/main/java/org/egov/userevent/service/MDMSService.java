package org.egov.userevent.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.userevent.config.PropertiesManager;
import org.egov.userevent.repository.RestCallRepository;
import org.egov.userevent.utils.ErrorConstants;
import org.egov.userevent.utils.UserEventsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

/**
 * Fetches the event masters from mdms-v2's header-authenticated GET API:
 *
 *   GET {mdmsHost}{mdmsV2Endpoint}?schemaCodes=mseva.EventTypes,mseva.EventCategories
 *   X-Tenant-ID: {state tenant}
 *   X-Client-ID: {configured client id}
 *   Authorization: Bearer {caller's token, forwarded from RequestInfo.authToken}
 *
 * Response envelope: {"mdms":[{schemaCode, uniqueIdentifier, data{code,...},
 * isActive, ...}]}. Only active records' data.code values are returned.
 */
@Service
@Slf4j
public class MDMSService {

	@Autowired
	private RestCallRepository repository;

	@Autowired
	private PropertiesManager props;

	/**
	 * Method to fetch event types and categories from mdms-v2
	 *
	 * @param requestInfo
	 * @param tenantId
	 * @return
	 */
	public Map<String, List<String>> fetchEventMasters(RequestInfo requestInfo, String tenantId) {
		String stateTenant = tenantId.split("\\.")[0]; // state-level master
		StringBuilder uri = new StringBuilder();
		uri.append(props.getMdmsHost()).append(props.getMdmsV2Endpoint())
				.append("?schemaCodes=").append(UserEventsConstants.MEN_MDMS_V2_EVENTTYPES_SCHEMA)
				.append(",").append(UserEventsConstants.MEN_MDMS_V2_EVENTCATEGORIES_SCHEMA);

		Optional<Object> response = repository.fetchResultWithHeaders(uri, stateTenant, props.getMdmsClientId(),
				null != requestInfo ? requestInfo.getAuthToken() : null);

		Map<String, List<String>> eventMasters = new HashMap<>();
		try {
			if (response.isPresent()) {
				List<String> codes = JsonPath.read(response.get(),
						UserEventsConstants.MEN_MDMS_V2_EVENTTYPES_CODES_JSONPATH);
				eventMasters.put(UserEventsConstants.MEN_MDMS_EVENTMASTER_CODE, codes);
				codes = JsonPath.read(response.get(),
						UserEventsConstants.MEN_MDMS_V2_EVENTCATEGORIES_CODES_JSONPATH);
				eventMasters.put(UserEventsConstants.MEN_MDMS_EVENTCATEGORY_MASTER_CODE, codes);
			} else
				throw new Exception();
		} catch (Exception e) {
			throw new CustomException(ErrorConstants.MEN_ERROR_FROM_MDMS_CODE, ErrorConstants.MEN_ERROR_FROM_MDMS_MSG);
		}
		return eventMasters;
	}

}
