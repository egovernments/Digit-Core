package org.egov.userevent.web.filter;

import java.io.IOException;

import org.egov.userevent.web.context.HeaderNames;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Sets the 3.0 contract's response headers on every endpoint: echoes
 * X-Request-ID / X-Correlation-ID / X-Tenant-ID from the request and adds
 * X-Response-Time (elapsed ms) and X-Response-Timestamp (epoch ms). The
 * response is wrapped so the timing headers can still be set after the
 * handler ran, before the body is committed.
 *
 * The X-Rate-Limit* headers declared in the spec are deliberately not set
 * here — rate limiting is enforced at the gateway, which owns that state.
 */
@Component
public class ResponseHeaderFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		long start = System.currentTimeMillis();
		echoHeader(request, response, HeaderNames.REQUEST_ID);
		echoHeader(request, response, HeaderNames.CORRELATION_ID);
		echoHeader(request, response, HeaderNames.TENANT_ID);

		ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
		try {
			filterChain.doFilter(request, wrappedResponse);
		} finally {
			long now = System.currentTimeMillis();
			wrappedResponse.setHeader(HeaderNames.RESPONSE_TIME, String.valueOf(now - start));
			wrappedResponse.setHeader(HeaderNames.RESPONSE_TIMESTAMP, String.valueOf(now));
			wrappedResponse.copyBodyToResponse();
		}
	}

	private void echoHeader(HttpServletRequest request, HttpServletResponse response, String header) {
		String value = request.getHeader(header);
		if (StringUtils.hasText(value)) {
			response.setHeader(header, value);
		}
	}
}
