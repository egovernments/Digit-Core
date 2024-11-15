package com.example.gateway.filters.pre.helpers;

import com.example.gateway.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class BlockingRateLimiter extends RedisRateLimiter {

	private static final String BLOCKED_KEY_PREFIX = "blocked:";

	@Value("${spring.rate.limiting.blocking.time.sec}")
	private long BLOCK_DURATION_SECONDS; // 5 minutes

	private final ReactiveStringRedisTemplate redisTemplate;

	private ApplicationProperties applicationProperties;

	public BlockingRateLimiter(ReactiveStringRedisTemplate redisTemplate, ApplicationProperties applicationProperties) {
		super(applicationProperties.getDefaultReplenishRate(), applicationProperties.getDefaultBurstCapacity()); // Default replenish rate and burst capacity (can be overridden)
		this.redisTemplate = redisTemplate;
	}

	@Override
	public Mono<Response> isAllowed(String routeId, String id) {
		// Check if the client is already blocked
		return redisTemplate.hasKey(BLOCKED_KEY_PREFIX + id)
				.flatMap(isBlocked -> {
					if (isBlocked) {
						// Create a rate limiter response with default headers
						log.info("Blocked: {}", id);
						Config defaultConfig = new Config().setReplenishRate(0).setBurstCapacity(0);
						return Mono.just(new Response(false, getHeaders(defaultConfig, 0L)));
					}
					// Proceed with default rate limiter
					return super.isAllowed(routeId, id).flatMap(response -> {
						if (!response.isAllowed()) {
							// Block the client if rate limit exceeded
							log.info("Block: {}", id);
							return blockClient(id).thenReturn(response);
						}
						log.info("Pass: {}", id);
						return Mono.just(response);
					});
				});
	}

	private Mono<Void> blockClient(String id) {
		return redisTemplate
				.opsForValue()
				.set(BLOCKED_KEY_PREFIX + id, "blocked", Duration.ofSeconds(BLOCK_DURATION_SECONDS))
				.then();
	}
}
