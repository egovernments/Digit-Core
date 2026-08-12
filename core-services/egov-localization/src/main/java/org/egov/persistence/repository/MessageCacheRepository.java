package org.egov.persistence.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.domain.model.Message;
import org.egov.domain.model.Tenant;
import org.egov.persistence.dto.MessageCacheEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@Service
public class MessageCacheRepository {

	private static final String MESSAGES_HASH_KEY = "messages";
	private static final String COMPUTED_MESSAGES_HASH_KEY = "computedMessages";
	/** Largest serialised cache entry to read or write, in bytes. 0 disables the cap. */
	@org.springframework.beans.factory.annotation.Value("${localization.cache.max.entry.bytes:10485760}")
	private long maxCacheEntryBytes;

	private StringRedisTemplate stringRedisTemplate;
	private ObjectMapper objectMapper;
    public static final Logger logger = LoggerFactory.getLogger(MessageCacheRepository.class);

	public MessageCacheRepository(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.objectMapper = objectMapper;
	}

	public List<Message> getComputedMessages(String locale, Tenant tenant, String module) {
		return getMessages(locale, tenant, COMPUTED_MESSAGES_HASH_KEY, module);
	}

	public void cacheComputedMessages(String locale, Tenant tenant, List<Message> messages, String module) {
		putMessages(locale, tenant, COMPUTED_MESSAGES_HASH_KEY, messages, module);
	}

	public List<Message> getMessages(String locale, Tenant tenant, String module) {
		return getMessages(locale, tenant, MESSAGES_HASH_KEY, module);
	}

	public void cacheMessages(String locale, Tenant tenant, List<Message> messages, String module) {
		putMessages(locale, tenant, MESSAGES_HASH_KEY, messages, module);
	}

	public void bustCache() {
		stringRedisTemplate.delete(MESSAGES_HASH_KEY);
		bustAllComputedMessagesCache();
	}

	public void bustCacheEntry(String locale, Tenant tenant, String module) {
		bustRawMessagesCacheEntry(locale, tenant, module);
		bustComputedMessagesCache(locale, tenant, module);
	}

    private void bustRawMessagesCacheEntry(String locale, Tenant tenant, String updatedModule) {

        // Fetch all keys inside "messages" hash
        var allKeys = stringRedisTemplate.opsForHash().keys(MESSAGES_HASH_KEY);

        if (allKeys == null) return;

        for (Object keyObj : allKeys) {
            String key = keyObj.toString();

            // key format: <locale>:<tenant>:<module or modules>
            // we clear any key where modules contain updatedModule
            if (key.contains(updatedModule)) {
                stringRedisTemplate.opsForHash().delete(MESSAGES_HASH_KEY, key);
            }
        }
    }


	private void bustComputedMessagesCache(String locale, Tenant tenant,String module) {
		if (tenant.isDefaultTenant()) {
			bustAllComputedMessagesCache();
		} else {
			deleteMatchingSubTenantCacheKeys(locale, tenant, module);
		}
	}

	private void bustAllComputedMessagesCache() {
		stringRedisTemplate.delete(COMPUTED_MESSAGES_HASH_KEY);
	}

	private void deleteMatchingSubTenantCacheKeys(String locale, Tenant tenant, String module) {
		getMatchingSubTenantCacheKeys(locale, tenant, module).forEach(this::deleteComputedMessageCacheEntry);
	}

	private Long deleteComputedMessageCacheEntry(String cacheKey) {
		return stringRedisTemplate.opsForHash().delete(COMPUTED_MESSAGES_HASH_KEY, cacheKey);
	}

	private Stream<String> getMatchingSubTenantCacheKeys(String locale, Tenant tenant, String module) {
		String parentCacheKey = getKey(locale, tenant.getTenantId(),module);
		return getAllComputedMessageCacheKeys().filter(key -> key.contains(parentCacheKey)|| key.startsWith(locale + ":" + tenant.getTenantId()));
	}

	private Stream<String> getAllComputedMessageCacheKeys() {
		return stringRedisTemplate.opsForHash().keys(COMPUTED_MESSAGES_HASH_KEY).stream().map(key -> (String) key);
	}

	private List<Message> getMessages(String locale, Tenant tenant, String hashKey, String module) {
		String messageKey = getKey(locale, tenant.getTenantId(), module);

		// Check the SIZE before reading the value. The cached entry is itself the hazard:
		// on unified-uat 2026-08-12 the module-absent entry fr_IN:mz had reached
		// 132,149,947 bytes, and merely deserialising it exhausted a 6 GB heap - the pod
		// was OOMKilled/137. HSTRLEN is O(1) and does not transfer the value.
		// An oversized entry is evicted (TTL is -1 on these hashes, so it is otherwise
		// permanent) and treated as a miss, handing control to the row-count guard on the
		// DB path. This makes the fix self-healing: no manual Redis flush at deployment.
		if (maxCacheEntryBytes > 0) {
			final Long size = stringRedisTemplate.opsForHash().lengthOfValue(hashKey, messageKey);
			if (size != null && size > maxCacheEntryBytes) {
				logger.warn("Refusing to deserialise oversized localisation cache entry {} in {}: "
						+ "{} bytes exceeds the {} byte limit; evicting", messageKey, hashKey, size,
						maxCacheEntryBytes);
				stringRedisTemplate.opsForHash().delete(hashKey, messageKey);
				return null;
			}
		}

		final String entry = (String) stringRedisTemplate.opsForHash().get(hashKey, messageKey);
		if (entry != null) {
			final MessageCacheEntry messageCacheEntry;
			try {
				messageCacheEntry = objectMapper.readValue(entry, MessageCacheEntry.class);
				return messageCacheEntry.getDomainMessages();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	private void putMessages(String locale, Tenant tenant, String hashKey, List<Message> messages, String module) {
		String messageKey = getKey(locale, tenant.getTenantId(), module);
		final MessageCacheEntry messageCacheEntry = new MessageCacheEntry(messages);
		try {
			final String cacheEntry = objectMapper.writeValueAsString(messageCacheEntry);
			final long sizeBytes = cacheEntry.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
			if (maxCacheEntryBytes > 0 && sizeBytes > maxCacheEntryBytes) {
				// Never create the hazard in the first place.
				logger.warn("Not caching localisation entry {} in {}: {} bytes exceeds the {} byte limit",
						messageKey, hashKey, sizeBytes, maxCacheEntryBytes);
				return;
			}
			stringRedisTemplate.opsForHash().put(hashKey, messageKey, cacheEntry);
		} catch (JsonProcessingException e) {
			logger.error("Exception occurred while processing JSON: " + e.getMessage());
		}
	}

	private String getKey(String locale, String tenant, String module) {
        if (module == null || module.isBlank()) {
            return String.format("%s:%s", locale, tenant);  // fallback without module
        }
        return String.format("%s:%s:%s", locale, tenant, module);
	}

}

