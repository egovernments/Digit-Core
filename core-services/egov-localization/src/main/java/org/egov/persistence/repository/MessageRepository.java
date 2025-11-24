package org.egov.persistence.repository;

import org.egov.domain.model.AuthenticatedUser;
import org.egov.domain.model.Message;
import org.egov.domain.model.Tenant;
import org.egov.tracer.model.CustomException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageRepository {

	private final MessageJpaRepository messageJpaRepository;

	public MessageRepository(MessageJpaRepository messageJpaRepository) {
		this.messageJpaRepository = messageJpaRepository;
	}

    @Transactional(readOnly = true)
    public List<Message> findByTenantIdAndLocaleAndModule(Tenant tenant, String locale, String module) {

        // If no module given → fallback to old logic
        if (module == null || module.trim().isEmpty()) {
            return messageJpaRepository.find(tenant.getTenantId(), locale)
                .map(org.egov.persistence.entity.Message::toDomain)
                .collect(Collectors.toList());
        }

        // Support multiple modules
        List<String> modules = List.of(module.split(","));

        return modules.stream()
            .flatMap(m -> messageJpaRepository.find(tenant.getTenantId(), locale, m)
                .stream()
                .map(org.egov.persistence.entity.Message::toDomain))
            .collect(Collectors.toList());
    }


	public List<Message> findAllMessage(Tenant tenant, String locale, String module, String code) {
		return messageJpaRepository.find(tenant.getTenantId(), locale, module, code).stream()
				.map(org.egov.persistence.entity.Message::toDomain).collect(Collectors.toList());
	}

	
	public void setUUID(List<org.egov.persistence.entity.Message> entityMessages){
		for(org.egov.persistence.entity.Message message : entityMessages){
			message.setId(UUID.randomUUID().toString());
		}
	}
	
	public void save(List<Message> messages, AuthenticatedUser authenticatedUser) {
		final List<org.egov.persistence.entity.Message> entityMessages = messages.stream()
				.map(org.egov.persistence.entity.Message::new).collect(Collectors.toList());
		setAuditFieldsForCreate(authenticatedUser, entityMessages);
		//Setting ID in UUID
		setUUID(entityMessages);
		try {
			messageJpaRepository.saveAll(entityMessages);
		} catch (DataIntegrityViolationException ex) {
			new DataIntegrityViolationExceptionTransformer(ex).transform();
		}
	}

	public void delete(String tenant, String locale, String module, List<String> codes) {
		final List<org.egov.persistence.entity.Message> messages = messageJpaRepository.find(tenant, locale, module,
				codes);
		if (CollectionUtils.isEmpty(messages)) {
			return;
		}
		messageJpaRepository.deleteAll(messages);
	}

	public void update(String tenant, String locale, String module, List<Message> domainMessages,
			AuthenticatedUser authenticatedUser) {
		final List<String> codes = getCodes(domainMessages);
		final List<org.egov.persistence.entity.Message> entityMessages = fetchMatchEntityMessages(tenant, locale,
				module, codes);
		updateMessages(domainMessages, entityMessages, authenticatedUser);
	}

	public void upsert(String tenant, String locale, String module, List<Message> domainMessages,
			AuthenticatedUser authenticatedUser) {
		final List<String> codes = getCodes(domainMessages);
		final List<org.egov.persistence.entity.Message> entityMessages = fetchMatchEntityMessages(tenant, locale,
				module, codes);
		List<String> newCodes = getNewCodes(entityMessages);
		List<Message> newMsgList = domainMessages.stream().filter(msg -> !newCodes.contains(msg.getCode()))
				.collect(Collectors.toList());
		save(newMsgList, authenticatedUser);

		updateMessages(domainMessages, entityMessages, authenticatedUser);

	}


	private void setAuditFieldsForCreate(AuthenticatedUser authenticatedUser,
			List<org.egov.persistence.entity.Message> entityMessages) {
		entityMessages.forEach(message -> {
			message.setCreatedDate(new Date());
			message.setCreatedBy(authenticatedUser.getUserId());
		});
	}

	private List<org.egov.persistence.entity.Message> fetchMatchEntityMessages(String tenant, String locale,
			String module, List<String> codes) {
		return messageJpaRepository.find(tenant, locale, module, codes);
	}

	private void updateMessages(List<Message> domainMessages, List<org.egov.persistence.entity.Message> entityMessages,
			AuthenticatedUser authenticatedUser) {
		final Map<String, Message> codeToMessageMap = getCodeToMessageMap(domainMessages);
		entityMessages.stream().forEach(entityMessage -> {
			final Message matchingMessage = codeToMessageMap.get(entityMessage.getCode());
			entityMessage.update(matchingMessage);
			setAuditFieldsForUpdate(authenticatedUser, entityMessage);

		});
		messageJpaRepository.saveAll(entityMessages);
	}

	private void setAuditFieldsForUpdate(AuthenticatedUser authenticatedUser,
			org.egov.persistence.entity.Message entityMessage) {
		entityMessage.setLastModifiedBy(authenticatedUser.getUserId());
		entityMessage.setLastModifiedDate(new Date());
	}

	private Map<String, Message> getCodeToMessageMap(List<Message> messages) {
		try{
			return messages.stream().collect(Collectors.toMap(Message::getCode, message -> message));
		}catch (Exception e){
			throw new CustomException("DUPLICATE_RECORDS",e.getMessage());
		}
	}

	private List<String> getCodes(List<Message> messages) {
		return messages.stream().map(Message::getCode).collect(Collectors.toList());
	}

	private List<String> getNewCodes(List<org.egov.persistence.entity.Message> messages) {
		return messages.stream().map(org.egov.persistence.entity.Message::getCode).collect(Collectors.toList());
	}
}
