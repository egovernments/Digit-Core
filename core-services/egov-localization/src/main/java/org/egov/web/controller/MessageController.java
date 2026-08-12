package org.egov.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.egov.domain.model.MessageRequest;
import org.egov.domain.model.MessageSearchCriteria;
import org.egov.domain.model.Tenant;
import org.egov.domain.service.MessageService;
import org.egov.producer.LocalizationProducer;
import org.egov.web.contract.*;
import org.egov.web.exception.InvalidMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping("/messages")
public class MessageController {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessageController.class);

	private MessageService messageService;

	/**
	 * Routes _upsert through the single-threaded consumer. ON by default: it fixes the
	 * 23505 race where concurrent upserts of the same key both insert and the loser's whole
	 * chunk is rejected with a 400.
	 *
	 * <p>A 200 still means COMMITTED - the request is held open until the consumer confirms
	 * - so callers that read their own writes back are not racing it.
	 */
	@Value("${localization.upsert.async.enabled:true}")
	private boolean asyncUpsertEnabled;

	@Autowired(required = false)
	private LocalizationProducer localizationProducer;

	public MessageController(MessageService messageService) {
		this.messageService = messageService;
	}

	@GetMapping()
	public MessagesResponse getMessagesForLocale(@RequestParam("locale") String locale,
			@RequestParam(value = "module", required = false) String module,
			@RequestParam("tenantId") String tenantId,@RequestParam(value = "codes",required = false) Set<String> codes) {
		return getMessages(locale, module, tenantId, codes);
	}

	@PostMapping("/v1/_search")
	public MessagesResponse getMessages(@RequestParam("locale") String locale,
                                        @RequestParam(value = "module", required = false)  String module,
                                        @RequestParam("tenantId") @Size(max = 256) String tenantId, @RequestParam(value = "codes",required = false) Set<String> codes) {
		final MessageSearchCriteria searchCriteria = MessageSearchCriteria.builder().locale(locale)
				.tenantId(new Tenant(tenantId)).codes(codes).module(module).build();
		List<org.egov.domain.model.Message> domainMessages = messageService.getFilteredMessages(searchCriteria);
		return createResponse(domainMessages);
	}
	
	@PostMapping("/v2/_search")
	public MessagesResponse getMessages(@RequestBody MessageRequest messageRequest) {
		
		List<org.egov.domain.model.Message> domainMessages = messageService.getFilteredMessages(messageRequest.getMessageSearchCriteria());
		return createResponse(domainMessages);
	}

	@PostMapping("/v1/_upsert")
	public MessagesResponse upsertMessages(@Valid @RequestBody CreateMessagesRequest messageRequest,
			BindingResult bindingResult) {

		if (bindingResult.hasErrors())
			throw new InvalidMessageRequest(bindingResult.getFieldErrors());

		final List<org.egov.domain.model.Message> messages = messageRequest.toDomainMessages();

		if (asyncUpsertEnabled && localizationProducer != null) {
			// Validate the caller BEFORE queueing, otherwise an unauthenticated request
			// would be accepted here and only rejected on a consumer thread.
			messageRequest.getAuthenticatedUser();
			try {
				// Blocks until the consumer confirms the commit - a 200 means committed.
				localizationProducer.pushUpsertAndAwaitCommit(toEvent(messageRequest));
				return createResponse(messages);
			} catch (RuntimeException e) {
				// FALL BACK TO THE DIRECT WRITE - i.e. exactly the behaviour before this
				// feature existed. A Kafka outage must not take localisation writes down
				// with it; the queue is an optimisation for write concurrency, not a
				// correctness requirement.
				//
				// Safe to retry directly even if the queued record is eventually delivered
				// and applied too: the upsert is idempotent on the
				// (tenantid, locale, module, code) unique constraint, so a double-apply is
				// a no-op. The producer marks itself degraded on failure, so subsequent
				// requests skip Kafka immediately instead of each paying the reply timeout.
				log.warn("Localisation upsert queue unavailable, writing directly: {}", e.getMessage());
			}
		}

		messageService.upsert(messageRequest.getTenant(), messages, messageRequest.getAuthenticatedUser());
		return createResponse(messages);
	}

	private UpsertMessagesEvent toEvent(CreateMessagesRequest messageRequest) {
		List<UpsertMessagesEvent.MessagePayload> payload = messageRequest.getMessages().stream()
				.map(m -> UpsertMessagesEvent.MessagePayload.builder().code(m.getCode()).message(m.getMessage())
						.module(m.getModule()).locale(m.getLocale()).build())
				.collect(Collectors.toList());
		return UpsertMessagesEvent.builder().requestInfo(messageRequest.getRequestInfo())
				.tenantId(messageRequest.getTenant().getTenantId()).messages(payload).build();
	}

	@PostMapping("/v1/_create")
	public MessagesResponse createMessages(@Valid @RequestBody CreateMessagesRequest messageRequest,
			BindingResult bindingResult) {

		if (bindingResult.hasErrors())
			throw new InvalidMessageRequest(bindingResult.getFieldErrors());

		final List<org.egov.domain.model.Message> messages = messageRequest.toDomainMessages();
        final String module  = messageRequest.getTenantId();
		messageService.create(messageRequest.getTenant(), messages, module,  messageRequest.getAuthenticatedUser());
		return createResponse(messages);
	}

	@PostMapping("/cache-bust")
	public CacheBustResponse clearMessagesCache() {
		messageService.bustCache();
		return new CacheBustResponse(null, true);
	}

	private MessagesResponse createResponse(List<org.egov.domain.model.Message> domainMessages) {
		return new MessagesResponse(domainMessages.stream().map(Message::new).collect(Collectors.toList()));
	}

	@PostMapping(value = "/v1/_update")
	public MessagesResponse update(@RequestBody @Valid final UpdateMessageRequest messageRequest,
			final BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			throw new InvalidMessageRequest(bindingResult.getFieldErrors());
		}
		final List<org.egov.domain.model.Message> messages = messageRequest.toDomainMessages();
		messageService.updateMessagesForModule(messageRequest.getTenant(), messages,
				messageRequest.getAuthenticatedUser());
		return createResponse(messages);
	}

	@PostMapping(value = "/v1/_delete")
	public DeleteMessagesResponse delete(@RequestBody @Valid final DeleteMessagesRequest deleteMessagesRequest,
			final BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			throw new InvalidMessageRequest(bindingResult.getFieldErrors());
		}
		messageService.delete(deleteMessagesRequest.getMessageIdentities());
		return new DeleteMessagesResponse(true);
	}

}