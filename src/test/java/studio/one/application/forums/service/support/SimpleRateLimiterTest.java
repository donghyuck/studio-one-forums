package studio.one.application.forums.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SimpleRateLimiterTest {

    @Test
    void blocksAfterConfiguredLimit() {
        SimpleRateLimiter limiter = new SimpleRateLimiter(Clock.fixed(Instant.parse("2026-03-10T00:00:00Z"), ZoneOffset.UTC));

        assertThat(limiter.tryAcquire("k", 2, java.time.Duration.ofMinutes(1))).isTrue();
        assertThat(limiter.tryAcquire("k", 2, java.time.Duration.ofMinutes(1))).isTrue();
        assertThat(limiter.tryAcquire("k", 2, java.time.Duration.ofMinutes(1))).isFalse();
    }
}
