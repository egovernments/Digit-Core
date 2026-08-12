package org.egov.persistence.repository;

import org.egov.domain.model.AuthenticatedUser;
import org.egov.domain.model.Message;
import org.egov.domain.model.Tenant;
import org.egov.tracer.model.CustomException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageRepository {

	/** Max rows a module-absent search may load. 0 disables the guard. */
	@org.springframework.beans.factory.annotation.Value("${localization.search.module.absent.max.messages:200000}")
	private long moduleAbsentMaxMessages;

	/**
	 * Atomic upsert. Conflict target is the unique_message_entry constraint
	 * (tenantid, locale, module, code) - the same one the old select-then-insert path
	 * raced against. createdby/createddate are preserved on conflict; only the message
	 * text and the lastmodified audit fields move.
	 */
	private static final String UPSERT_SQL =
			"INSERT INTO message (id, code, message, module, locale, tenantid, createdby, createddate, "
			+ "lastmodifiedby, lastmodifieddate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
			+ "ON CONFLICT (tenantid, locale, module, code) DO UPDATE SET "
			+ "message = EXCLUDED.message, lastmodifiedby = EXCLUDED.lastmodifiedby, "
			+ "lastmodifieddate = EXCLUDED.lastmodifieddate";

	private final JdbcTemplate jdbcTemplate;

	private final MessageJpaRepository messageJpaRepository;

	public MessageRepository(MessageJpaRepository messageJpaRepository, JdbcTemplate jdbcTemplate) {
		this.messageJpaRepository = messageJpaRepository;
		this.jdbcTemplate = jdbcTemplate;
	}

    @Transactional(readOnly = true)
    public List<Message> findByTenantIdAndLocaleAndModule(Tenant tenant, String locale, String module) {

        // If no module given → fallback to old logic
        if (module == null || module.trim().isEmpty()) {
            // GUARD: this is the query that killed unified-uat on 2026-08-12. ONE _search
            // without a module, out of 22,425 queries the pod served, ran
            // WHERE tenantid=? AND locale=? over fr_IN/mz, hung 57 s, threw
            // OutOfMemoryError twice and the pod was OOMKilled/137 on a 6 GB heap.
            // Streaming (already present above) bounds the driver's buffering but not the
            // materialised result, so the row count is bounded here too. No heap size
            // fixes an unbounded read; refusing it keeps the pod alive for everyone else.
            guardModuleAbsentRead(tenant, locale);
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


	private void guardModuleAbsentRead(Tenant tenant, String locale) {
		if (moduleAbsentMaxMessages <= 0) {
			return;
		}
		final long count = messageJpaRepository.countByTenantIdAndLocale(tenant.getTenantId(), locale);
		if (count > moduleAbsentMaxMessages) {
			throw new CustomException("LOCALIZATION_MODULE_REQUIRED", String.format(
					"Refusing an unbounded localisation search: tenant=%s locale=%s has %d messages, "
							+ "above the limit of %d. Pass a 'module' parameter to scope the request.",
					tenant.getTenantId(), locale, count, moduleAbsentMaxMessages));
		}
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

	/**
	 * Atomic upsert. One batched statement per chunk, no read-modify-write.
	 *
	 * <p>REPLACES a select-then-insert sequence that had a time-of-check/time-of-use race:
	 * two concurrent writers of the same (tenantid, locale, module, code) both saw "not
	 * present", both inserted, and the loser took a 23505 unique violation which rejected
	 * its ENTIRE chunk with a 400. Measured on the old path: 12 concurrent requests
	 * carrying the same 400 codes produced 2 failed requests and 6 constraint violations.
	 *
	 * <p>{@code ON CONFLICT ... DO UPDATE} makes the operation atomic in the database, so
	 * the race is not serialised away - it cannot occur. That is strictly stronger than
	 * funnelling writes through a single consumer thread, because it also holds across
	 * replicas, rebalances and any number of concurrent writers.
	 *
	 * <p>It also removes the per-row existence SELECT. The unified-uat census showed 22,136
	 * such selects sitting next to 22,136 inserts; this halves the round trips.
	 */
	@Transactional
	public void upsert(String tenant, String locale, String module, List<Message> domainMessages,
			AuthenticatedUser authenticatedUser) {
		if (CollectionUtils.isEmpty(domainMessages)) {
			return;
		}
		final Date now = new Date();
		final Long userId = authenticatedUser.getUserId();
		jdbcTemplate.batchUpdate(UPSERT_SQL, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				final Message m = domainMessages.get(i);
				ps.setString(1, UUID.randomUUID().toString());
				ps.setString(2, m.getCode());
				ps.setString(3, m.getMessage());
				ps.setString(4, m.getModule());
				ps.setString(5, m.getLocale());
				ps.setString(6, m.getTenant());
				ps.setLong(7, userId);
				ps.setTimestamp(8, new java.sql.Timestamp(now.getTime()));
				ps.setLong(9, userId);
				ps.setTimestamp(10, new java.sql.Timestamp(now.getTime()));
			}

			@Override
			public int getBatchSize() {
				return domainMessages.size();
			}
		});
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

}
