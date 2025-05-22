package com.example.rate_limiter;

import com.example.rate_limiter.service.impl.FixedWindowRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class RateLimiterApplicationTests {

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private FixedWindowRateLimiter rateLimiter;

	@BeforeEach
	void setUp() {
		try {
			Field field = FixedWindowRateLimiter.class.getDeclaredField("RATE_LIMIT_MAX_REQUESTS");
			field.setAccessible(true);
			field.set(rateLimiter, 100L);
		} catch (Exception e) {
			throw new RuntimeException("Failed to inject RATE_LIMIT_MAX_REQUESTS", e);
		}
	}

	@Test
	void allowRequest_shouldAllowWithinLimit() {
		String userId = "user1";
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.increment(anyString())).thenReturn(1L);

		boolean allowed = rateLimiter.allowRequest(userId);
		assertTrue(allowed);
	}

	@Test
	void allowRequest_shouldRejectWhenLimitExceeded() {
		String userId = "user2";
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.increment(anyString())).thenReturn(101L);

		boolean allowed = rateLimiter.allowRequest(userId);
		assertFalse(allowed);
	}

	@Test
	void concurrencyTest_allowAndRateLimitCounts() throws InterruptedException {
		int totalRequests = 150;
		int rateLimit = 100;

		AtomicInteger redisCounter = new AtomicInteger(0);
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger rateLimitedCount = new AtomicInteger(0);

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.increment(anyString()))
				.thenAnswer(invocation -> (long) redisCounter.incrementAndGet());

		ExecutorService executor = Executors.newFixedThreadPool(20);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(totalRequests);

		Runnable task = () -> {
			try {
				startLatch.await();
				boolean allowed = rateLimiter.allowRequest("user123");
				if (allowed) successCount.incrementAndGet();
				else rateLimitedCount.incrementAndGet();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				doneLatch.countDown();
			}
		};

		for (int i = 0; i < totalRequests; i++) {
			executor.submit(task);
		}

		startLatch.countDown();
		doneLatch.await();
		executor.shutdown();

		assertEquals(rateLimit, successCount.get());
		assertEquals(totalRequests - rateLimit, rateLimitedCount.get());
	}


}
