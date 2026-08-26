/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) 2016  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.userevent.web.controller;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.userevent.service.UserEventsService;
import org.egov.userevent.utils.ErrorConstants;
import org.egov.userevent.web.contract.Event;
import org.egov.userevent.web.contract.EventRequest;
import org.egov.userevent.web.contract.EventResponse;
import org.egov.userevent.web.contract.EventSearchCriteria;
import org.egov.userevent.web.context.GatewayRequestInfoFactory;
import org.egov.userevent.web.context.HeaderNames;
import org.egov.userevent.web.contract.v3.EventListRequest;
import org.egov.userevent.web.contract.v3.EventStatusV3;
import org.egov.userevent.web.contract.v3.EventV3;
import org.egov.userevent.web.error.ApiExceptionHandler;
import org.egov.userevent.web.mapper.EventApiMapper;
import org.egov.userevent.web.mapper.EventUpdateMerger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 3.0 contract event endpoints. Tenant scoping comes from the X-Tenant-ID
 * header and caller identity from X-User-ID (gateway-populated); request and
 * response bodies follow user-events-3.0.yml — no RequestInfo wrapper, bare
 * arrays of events on the way out.
 *
 * The 3.0 contract cannot express the legacy CITIZEN/EMPLOYEE user types, so
 * _search behaves as follows: when the caller supplies userIds or roles query
 * params the search runs in citizen mode ("my notifications" — the service
 * replaces those filters with the caller's own uuid and expands the recipient
 * registry); otherwise it runs as a tenant-scoped employee search.
 */
@RestController
@Validated
@RequestMapping("/v1/events")
public class UserEventsController {

	private static final String TYPE_CITIZEN = "CITIZEN";
	private static final String TYPE_EMPLOYEE = "EMPLOYEE";

	@Autowired
	private UserEventsService service;

	@Autowired
	private EventApiMapper mapper;

	@Autowired
	private EventUpdateMerger updateMerger;

	@Autowired
	private GatewayRequestInfoFactory requestInfoFactory;

	/**
	 * Creates one or more events under the calling tenant. Returns 201 with the
	 * enriched events as a bare array.
	 */
	@PostMapping("/_create")
	public ResponseEntity<List<EventV3>> create(@RequestHeader(HeaderNames.TENANT_ID) String tenantId,
			@RequestBody @Valid EventListRequest request, HttpServletRequest httpRequest) {
		requireUserId(httpRequest);
		RequestInfo requestInfo = requestInfoFactory.from(httpRequest, TYPE_EMPLOYEE);
		EventRequest eventRequest = EventRequest.builder().requestInfo(requestInfo)
				.events(mapper.toInternal(request.getEvents(), tenantId)).build();
		EventResponse response = service.createEvents(eventRequest, false);
		return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toApi(response.getEvents()));
	}

	/**
	 * Updates one or more existing events under the calling tenant. Internal-only
	 * fields the 3.0 contract dropped are restored from the stored events before
	 * the update runs.
	 */
	@PostMapping("/_update")
	public ResponseEntity<List<EventV3>> update(@RequestHeader(HeaderNames.TENANT_ID) String tenantId,
			@RequestBody @Valid EventListRequest request, HttpServletRequest httpRequest) {
		requireUserId(httpRequest);
		RequestInfo requestInfo = requestInfoFactory.from(httpRequest, TYPE_EMPLOYEE);
		List<Event> events = mapper.toInternal(request.getEvents(), tenantId);
		updateMerger.merge(events, tenantId);
		EventRequest eventRequest = EventRequest.builder().requestInfo(requestInfo).events(events).build();
		EventResponse response = service.updateEvents(eventRequest);
		return ResponseEntity.ok(mapper.toApi(response.getEvents()));
	}

	/**
	 * Returns events under the calling tenant matching the given filters as a
	 * bare array.
	 */
	@PostMapping("/_search")
	public ResponseEntity<List<EventV3>> search(@RequestHeader(HeaderNames.TENANT_ID) String tenantId,
			@RequestParam(required = false) List<String> ids,
			@RequestParam(required = false) EventStatusV3 status,
			@RequestParam(required = false) List<String> name,
			@RequestParam(required = false) List<String> eventTypes,
			@RequestParam(required = false) List<String> roles,
			@RequestParam(required = false) List<String> userIds,
			@RequestParam(required = false) List<String> postedBy,
			@RequestParam(required = false) Long fromDate,
			@RequestParam(required = false) Long toDate,
			@RequestParam(required = false, defaultValue = "200") @Min(1) @Max(200) Integer limit,
			@RequestParam(required = false, defaultValue = "0") @Min(0) Integer offset,
			HttpServletRequest httpRequest) {
		if (null != limit && (limit < 1 || limit > 200)) {
			throw new CustomException(ApiExceptionHandler.INVALID_REQUEST, "limit must be between 1 and 200");
		}
		if (null != offset && offset < 0) {
			throw new CustomException(ApiExceptionHandler.INVALID_REQUEST, "offset must not be negative");
		}
		boolean citizenMode = !CollectionUtils.isEmpty(userIds) || !CollectionUtils.isEmpty(roles);
		RequestInfo requestInfo = requestInfoFactory.from(httpRequest, citizenMode ? TYPE_CITIZEN : TYPE_EMPLOYEE);
		EventSearchCriteria criteria = mapper.toSearchCriteria(tenantId, ids, status, name, eventTypes, roles,
				userIds, postedBy, fromDate, toDate, limit, offset);
		EventResponse response = service.searchEvents(requestInfo, criteria, false);
		return ResponseEntity.ok(mapper.toApi(response.getEvents()));
	}

	private void requireUserId(HttpServletRequest httpRequest) {
		if (!StringUtils.hasText(httpRequest.getHeader(HeaderNames.USER_ID))) {
			throw new CustomException(ErrorConstants.MISSING_ROLE_USERID_CODE,
					"The " + HeaderNames.USER_ID + " header is mandatory for this operation");
		}
	}
}
