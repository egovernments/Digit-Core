package org.egov.userevent.web.controller;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.userevent.model.LastAccesDetails;
import org.egov.userevent.service.UserEventsService;
import org.egov.userevent.utils.ErrorConstants;
import org.egov.userevent.web.contract.EventSearchCriteria;
import org.egov.userevent.web.contract.NotificationCountResponse;
import org.egov.userevent.web.context.GatewayRequestInfoFactory;
import org.egov.userevent.web.context.HeaderNames;
import org.egov.userevent.web.contract.v3.LastAccessTimeRequest;
import org.egov.userevent.web.contract.v3.LastAccessTimeResponse;
import org.egov.userevent.web.contract.v3.NotificationCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 3.0 contract notification endpoints. These live outside the /v1/events
 * prefix per the spec. Both identify the calling user solely from the
 * X-User-ID header, which is therefore mandatory here — the notification
 * count and read/unread state are per-user concepts.
 */
@RestController
public class NotificationsController {

	private static final String TYPE_CITIZEN = "CITIZEN";

	@Autowired
	private UserEventsService service;

	@Autowired
	private GatewayRequestInfoFactory requestInfoFactory;

	/**
	 * Returns total/read/unread notification counts for the calling user under
	 * the calling tenant. read is derived as total - unread (the legacy count
	 * query computes only total and unread).
	 */
	@PostMapping("/notification/_count")
	public ResponseEntity<NotificationCount> count(@RequestHeader(HeaderNames.TENANT_ID) String tenantId,
			HttpServletRequest httpRequest) {
		requireUserId(httpRequest);
		RequestInfo requestInfo = requestInfoFactory.from(httpRequest, TYPE_CITIZEN);
		EventSearchCriteria criteria = new EventSearchCriteria();
		criteria.setTenantId(tenantId);
		NotificationCountResponse response = service.fetchCount(requestInfo, criteria);
		if (null == response || null == response.getTotalCount() || null == response.getUnreadCount()) {
			throw new IllegalStateException("Notification count could not be fetched");
		}
		return ResponseEntity.ok(NotificationCount.builder()
				.total(response.getTotalCount())
				.unread(response.getUnreadCount())
				.read(Math.max(0, response.getTotalCount() - response.getUnreadCount()))
				.build());
	}

	/**
	 * Records the calling user's last-access time, which determines which
	 * notifications count as read. The time defaults to the server clock when
	 * the optional body omits it.
	 */
	@PostMapping("/lat/_update")
	public ResponseEntity<LastAccessTimeResponse> updateLastAccessTime(
			@RequestHeader(HeaderNames.TENANT_ID) String tenantId,
			@RequestBody(required = false) LastAccessTimeRequest body, HttpServletRequest httpRequest) {
		requireUserId(httpRequest);
		RequestInfo requestInfo = requestInfoFactory.from(httpRequest, TYPE_CITIZEN);
		Long lastAccessTime = (null != body) ? body.getLastAccessTime() : null;
		LastAccesDetails details = service.persistLastAccessTime(requestInfo, lastAccessTime);
		return ResponseEntity.ok(LastAccessTimeResponse.builder()
				.userId(details.getUserId())
				.lastAccessTime(details.getLastAccessTime())
				.build());
	}

	private void requireUserId(HttpServletRequest httpRequest) {
		if (!StringUtils.hasText(httpRequest.getHeader(HeaderNames.USER_ID))) {
			throw new CustomException(ErrorConstants.MISSING_ROLE_USERID_CODE,
					"The " + HeaderNames.USER_ID + " header is mandatory for this operation");
		}
	}
}
