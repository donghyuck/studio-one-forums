package studio.one.application.forums.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
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

    @Test
    void allowsRequestsAgainAfterWindowExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-03-10T00:00:00Z"));
        SimpleRateLimiter limiter = new SimpleRateLimiter(clock);

        assertThat(limiter.tryAcquire("k", 1, Duration.ofMinutes(1))).isTrue();
        assertThat(limiter.tryAcquire("k", 1, Duration.ofMinutes(1))).isFalse();

        clock.advance(Duration.ofMinutes(2));

        assertThat(limiter.tryAcquire("k", 1, Duration.ofMinutes(1))).isTrue();
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
