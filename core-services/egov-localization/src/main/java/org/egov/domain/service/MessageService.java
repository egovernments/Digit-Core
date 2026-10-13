package org.egov.domain.service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.egov.domain.model.AuthenticatedUser;
import org.egov.domain.model.Message;
import org.egov.domain.model.MessageIdentity;
import org.egov.domain.model.MessageSearchCriteria;
import org.egov.domain.model.Tenant;
import org.egov.persistence.repository.MessageCacheRepository;
import org.egov.persistence.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Responsible for creating, updating and computing localization message list.
 *
 * For the search request: locale: mr_IN, tenant: mh.panvel the computed message
 * list is based on the below logic - 1) Retrieve messages for locale: en_IN and
 * tenant: default 2) Retrieve messages for locale: mr_IN and tenant mh 3)
 * Override messages of step 1 with messages from step 2. 4) Retrieve messages
 * from locale: mr_IN and tenant mh.panvel 5) Override messages from step 3 with
 * messages from step 4.
 *
 * The items cached in Redis are - 1) The final computed message list. 2) The
 * raw messages ready from PostGres for every locale and tenant combination.
 *
 * Cache bust logic - a) For a create/update request to locale: mr_IN and
 * tenant: mh.panvel - 1) In validate cache entry for raw messages with key
 * mr_IN:mh.panvel 2) In validate cache entry for computed messages with key
 * mr_IN:mh.panvel
 *
 * b) For a create/update request to locale: mr_IN and tenant: mh - 1) In
 * validate cache entry for raw messages with key mr_IN:mh 2) In validate cache
 * entry for computed messages with key mr_IN:mh.panvel and mr_IN:mh
 *
 * c) For a create/update request to locale: <locale> and tenant: default- 1) In
 * validate all computed messages entries. 2) In validate cache entry for raw
 * messages with key <locale>:default
 */
@Service
@Slf4j
public class MessageService {
	private static final String ENGLISH_INDIA = "en_IN";
	private MessageRepository messageRepository;
	private MessageCacheRepository messageCacheRepository;

	public MessageService(MessageRepository messageRepository, MessageCacheRepository messageCacheRepository) {
		this.messageRepository = messageRepository;
		this.messageCacheRepository = messageCacheRepository;
	}

	public void upsert(Tenant tenant, List<Message> messages, AuthenticatedUser user) {

		Message message = messages.get(0);
		messageRepository.upsert(message.getTenant(), message.getLocale(), message.getModule(), messages, user);
		bustCacheEntriesForMessages(tenant, messages);
	}

	public void create(Tenant tenant, List<Message> messages, AuthenticatedUser user) {
		messageRepository.save(messages, user);
		bustCacheEntriesForMessages(tenant, messages);
	}

	public void updateMessagesForModule(Tenant tenant, List<Message> messages, AuthenticatedUser user) {
		if (CollectionUtils.isEmpty(messages)) {
			throw new IllegalArgumentException("update message list cannot be empty");
		}
		final Message message = messages.get(0);
		messageRepository.update(message.getTenant(), message.getLocale(), message.getModule(), messages, user);
		bustCacheEntriesForMessages(tenant, messages);
	}

	public void bustCache() {
		messageCacheRepository.bustCache();
	}

	public List<Message> getFilteredMessages(MessageSearchCriteria searchCriteria) {
		List<Message> messages = getMessages(searchCriteria);

		Tenant tenant = searchCriteria.getTenantId();

		List<Tenant> tenants = tenant.getTenantHierarchy();

		List<Message> filteredMessages = new LinkedList<>();

		for (Tenant t : tenants){
		    filteredMessages = filterForGivenTenantId(t.getTenantId(), messages, searchCriteria);

		    if(!CollectionUtils.isEmpty(filteredMessages))
		        break;
        }

		return filteredMessages;
	}


	private List<Message> filterForGivenTenantId(String tenantId, List<Message> messages, MessageSearchCriteria searchCriteria){

        if (searchCriteria.isModuleAbsent() && !CollectionUtils.isEmpty(searchCriteria.getCodes())) {

            Set<String> codes = searchCriteria.getCodes();

            return messages.stream()
                .filter(e -> e.getLocale().equals(searchCriteria.getLocale())
                    && e.getTenant().equals(tenantId)
                    && codes.contains(e.getCode()))
                .collect(Collectors.toList());
        } else if(searchCriteria.isModuleAbsent() && CollectionUtils.isEmpty(searchCriteria.getCodes())){
            return messages.parallelStream()
                .filter(e -> e.getLocale().equals(searchCriteria.getLocale())
                    && e.getTenant().equals(tenantId))
                .collect(Collectors.toList());
        } else {
            List<String> modules = Arrays.asList(searchCriteria.getModule().split("[,]"));

            if(CollectionUtils.isEmpty(searchCriteria.getCodes()))
                messages = messages.stream()
                    .filter(message -> modules.contains(message.getModule())
                        && message.getLocale().equals(searchCriteria.getLocale())
                        && message.getTenant().equals(tenantId))
                    .collect(Collectors.toList());
            else {
                Set<String> codes = searchCriteria.getCodes();
                messages = messages.stream()
                    .filter(message -> modules.contains(message.getModule())
                        && codes.contains(message.getCode())
                        && message.getLocale().equals(searchCriteria.getLocale())
                        && message.getTenant().equals(tenantId))
                    .collect(Collectors.toList());
            }

        }
        return messages;

    }



	public void delete(List<MessageIdentity> messageIdentities) {
		final Map<Tenant, List<MessageIdentity>> tenantToMessageIdentitiesMap = messageIdentities.stream()
				.collect(Collectors.groupingBy(MessageIdentity::getTenant));
		tenantToMessageIdentitiesMap.keySet()
				.forEach(tenant -> deleteMessagesForGivenTenant(tenantToMessageIdentitiesMap, tenant));
	}

	private void bustCacheEntriesForMessages(Tenant tenant, List<Message> messages) {
		final List<MessageIdentity> messageIdentities = messages.stream().map(Message::getMessageIdentity)
				.collect(Collectors.toList());
		bustCacheEntriesForMessageIdentities(tenant, messageIdentities);
	}

	private void bustCacheEntriesForMessageIdentities(Tenant tenant, List<MessageIdentity> messageIdentities) {
		messageIdentities.stream().map(MessageIdentity::getLocale).distinct()
				.forEach(locale -> bustCacheEntry(tenant, locale));
	}

	private void bustCacheEntry(Tenant tenant, String locale) {
		messageCacheRepository.bustCacheEntry(locale, tenant);
	}

	/**
	 * Retrieves messages for a search request.
	 *
	 * When a module parameter is present, uses a module-scoped path that only
	 * loads messages for the requested modules from the database. This reduces
	 * memory usage from ~49K records (all modules) to ~5-8K records (single module),
	 * preventing OOM on large datasets.
	 *
	 * When no module is specified, falls back to the original full computation.
	 */
	private List<Message> getMessages(MessageSearchCriteria searchCriteria) {
		if (!searchCriteria.isModuleAbsent()) {
			return getMessagesModuleScoped(searchCriteria);
		}
		return getMessagesUnscoped(searchCriteria);
	}

	/**
	 * Module-scoped message retrieval. Queries the database with a module filter
	 * pushed down to SQL, avoiding loading all ~49K messages into memory.
	 * Uses a separate cache key that includes the module(s).
	 */
	private List<Message> getMessagesModuleScoped(MessageSearchCriteria searchCriteria) {
		String locale = searchCriteria.getLocale();
		Tenant tenant = searchCriteria.getTenantId();
		List<String> modules = Arrays.asList(searchCriteria.getModule().split("[,]"));

		// Check module-scoped computed cache first
		final List<Message> cachedMessages = messageCacheRepository.getComputedMessagesForModules(
				locale, tenant, modules);
		if (cachedMessages != null) {
			return cachedMessages;
		}

		// Compute using module-scoped DB queries (projection-based, no JPA entity overhead)
		final Collection<Message> messagesForLocale = getMessagesForGivenLocaleAndModules(locale, tenant, modules);
		List<Message> defaultMessages = getDefaultMessagesForMissingCodesWithModules(messagesForLocale, modules);
		List<Message> computedMessages = Stream.concat(messagesForLocale.stream(), defaultMessages.stream())
				.sorted(Comparator.comparing(Message::getCode)).collect(Collectors.toList());

		messageCacheRepository.cacheComputedMessagesForModules(locale, tenant, modules, computedMessages);
		return computedMessages;
	}

	/**
	 * Original unscoped message retrieval (no module filter).
	 * Now uses projection queries to avoid JPA persistence context overhead.
	 */
	private List<Message> getMessagesUnscoped(MessageSearchCriteria searchCriteria) {
		final List<Message> cachedMessages = messageCacheRepository.getComputedMessages(searchCriteria.getLocale(),
				searchCriteria.getTenantId());
		if (cachedMessages != null) {
			return cachedMessages;
		}
		final List<Message> computedMessages = computeMessageList(searchCriteria.getLocale(),
				searchCriteria.getTenantId());
		messageCacheRepository.cacheComputedMessages(searchCriteria.getLocale(), searchCriteria.getTenantId(),
				computedMessages);
		return computedMessages;
	}

	private void deleteMessagesForGivenTenant(Map<Tenant, List<MessageIdentity>> tenantToMessageIdentitiesMap,
			Tenant tenant) {
		final List<MessageIdentity> messageIdentitiesForGivenTenant = tenantToMessageIdentitiesMap.get(tenant);
		final Map<String, List<MessageIdentity>> localeToMessageIdentitiesMap = messageIdentitiesForGivenTenant.stream()
				.collect(Collectors.groupingBy(MessageIdentity::getLocale));
		localeToMessageIdentitiesMap.keySet()
				.forEach(locale -> deleteMessagesForGivenLocale(tenant, localeToMessageIdentitiesMap, locale));
		bustCacheEntriesForMessageIdentities(tenant, messageIdentitiesForGivenTenant);
	}

	private void deleteMessagesForGivenLocale(Tenant tenant,
			Map<String, List<MessageIdentity>> localeToMessageIdentitiesMap, String locale) {
		final List<MessageIdentity> messageIdentitiesForGivenLocale = localeToMessageIdentitiesMap.get(locale);
		final Map<String, List<MessageIdentity>> moduleToMessageIdentitiesMap = messageIdentitiesForGivenLocale.stream()
				.collect(Collectors.groupingBy(MessageIdentity::getModule));
		moduleToMessageIdentitiesMap.keySet()
				.forEach(module -> deleteMessagesForGivenModule(tenant, locale, moduleToMessageIdentitiesMap, module));
	}

	private void deleteMessagesForGivenModule(Tenant tenant, String locale,
			Map<String, List<MessageIdentity>> moduleToMessageIdentitiesMap, String module) {
		final List<MessageIdentity> messageIdentitiesForGivenModule = moduleToMessageIdentitiesMap.get(module);
		final List<String> codes = getCodesForGivenMessage(messageIdentitiesForGivenModule);
		messageRepository.delete(tenant.getTenantId(), locale, module, codes);
	}

	private List<String> getCodesForGivenMessage(List<MessageIdentity> messageIdentitiesForGivenModule) {
		return messageIdentitiesForGivenModule.stream().map(MessageIdentity::getCode).collect(Collectors.toList());
	}

	/**
	 * Original full computation — now uses projection queries to avoid JPA entity overhead.
	 */
	private List<Message> computeMessageList(String locale, Tenant tenant) {
		final Collection<Message> messagesForGivenLocale = getMessagesForGivenLocale(locale, tenant);
		List<Message> defaultMessages = getDefaultMessagesForMissingCodes(messagesForGivenLocale);
		return Stream.concat(messagesForGivenLocale.stream(), defaultMessages.stream())
				.sorted(Comparator.comparing(Message::getCode)).collect(Collectors.toList());
	}

	private List<Message> getDefaultMessagesForMissingCodes(Collection<Message> messagesForGivenLocale) {
		final List<Message> messagesInEnglishForDefaultTenant = fetchMessagesProjected(ENGLISH_INDIA,
				new Tenant(Tenant.DEFAULT_TENANT));

        Set<String> messageCodesInGivenLanguage = new HashSet<>();

        messagesForGivenLocale.forEach(message -> {
            messageCodesInGivenLanguage.add(message.getModule()+message.getCode());
        });

		return getEnglishMessagesForCodesNotPresentInLocalLanguage(messageCodesInGivenLanguage,
				messagesInEnglishForDefaultTenant);
	}

	/**
	 * Module-scoped version: only loads English defaults for the requested modules.
	 */
	private List<Message> getDefaultMessagesForMissingCodesWithModules(Collection<Message> messagesForGivenLocale,
			List<String> modules) {
		final List<Message> messagesInEnglishForDefaultTenant = fetchMessagesProjectedForModules(ENGLISH_INDIA,
				new Tenant(Tenant.DEFAULT_TENANT), modules);

		Set<String> messageCodesInGivenLanguage = new HashSet<>();
		messagesForGivenLocale.forEach(message -> {
			messageCodesInGivenLanguage.add(message.getModule() + message.getCode());
		});

		return getEnglishMessagesForCodesNotPresentInLocalLanguage(messageCodesInGivenLanguage,
				messagesInEnglishForDefaultTenant);
	}

	private Collection<Message> getMessagesForGivenLocale(String locale, Tenant tenant) {
		final Map<String, Message> codeToMessageMap = new HashMap<>();
		final List<Message> messages = tenant.getTenantHierarchy().stream()
				.map(tenantItem -> fetchMessagesProjected(locale, tenantItem)).flatMap(List::stream)
				.collect(Collectors.toList());

		messages.forEach(message -> {
			final Message matchingMessage = codeToMessageMap.get(message.getModule()+message.getCode());
			if (matchingMessage == null) {
				codeToMessageMap.put(message.getModule()+message.getCode(), message);
			} else {
				if (message.isMoreSpecificComparedTo(matchingMessage)) {
					codeToMessageMap.put(message.getModule()+message.getCode(), message);
				}
			}
		});

		return codeToMessageMap.values();
	}

	/**
	 * Module-scoped tenant hierarchy merge — only loads messages for the specified modules.
	 */
	private Collection<Message> getMessagesForGivenLocaleAndModules(String locale, Tenant tenant,
			List<String> modules) {
		final Map<String, Message> codeToMessageMap = new HashMap<>();
		final List<Message> messages = tenant.getTenantHierarchy().stream()
				.map(tenantItem -> fetchMessagesProjectedForModules(locale, tenantItem, modules))
				.flatMap(List::stream)
				.collect(Collectors.toList());

		messages.forEach(message -> {
			final Message matchingMessage = codeToMessageMap.get(message.getModule() + message.getCode());
			if (matchingMessage == null) {
				codeToMessageMap.put(message.getModule() + message.getCode(), message);
			} else {
				if (message.isMoreSpecificComparedTo(matchingMessage)) {
					codeToMessageMap.put(message.getModule() + message.getCode(), message);
				}
			}
		});

		return codeToMessageMap.values();
	}

	private List<Message> getEnglishMessagesForCodesNotPresentInLocalLanguage(Set<String> messageCodesForGivenLocale,
			List<Message> messagesInEnglish) {
		return messagesInEnglish.stream().filter(message -> !messageCodesForGivenLocale.contains(message.getModule()+message.getCode()))
				.collect(Collectors.toList());
	}

	/**
	 * Fetch messages using projection queries (no JPA persistence context overhead).
	 * Cached in Redis per locale+tenant (raw message cache).
	 */
	private List<Message> fetchMessagesProjected(String locale, Tenant tenant) {
		final List<Message> cachedMessages = messageCacheRepository.getMessages(locale, tenant);
		if (cachedMessages != null) {
			return cachedMessages;
		}
		final List<Message> messages = messageRepository.findProjectedByTenantAndLocale(tenant, locale);
		messageCacheRepository.cacheMessages(locale, tenant, messages);
		return messages;
	}

	/**
	 * Fetch messages for specific modules using projection queries.
	 * Uses a module-scoped cache key to avoid polluting or being polluted by the unscoped cache.
	 */
	private List<Message> fetchMessagesProjectedForModules(String locale, Tenant tenant, List<String> modules) {
		final List<Message> cachedMessages = messageCacheRepository.getMessagesForModules(locale, tenant, modules);
		if (cachedMessages != null) {
			return cachedMessages;
		}
		final List<Message> messages = messageRepository.findProjectedByTenantLocaleAndModules(tenant, locale, modules);
		messageCacheRepository.cacheMessagesForModules(locale, tenant, modules, messages);
		return messages;
	}

}
