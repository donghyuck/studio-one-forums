package studio.one.application.forums.service.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class SimpleRateLimiter {
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public SimpleRateLimiter() {
        this(Clock.systemUTC());
    }

    SimpleRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public boolean tryAcquire(String key, int limit, Duration window) {
        if (limit <= 0) {
            return true;
        }
        Instant now = clock.instant();
        Window current = windows.compute(key, (ignored, existing) -> refresh(existing, now, window));
        synchronized (current) {
            if (current.count >= limit) {
                return false;
            }
            current.count += 1;
            return true;
        }
    }

    private Window refresh(Window existing, Instant now, Duration duration) {
        if (existing == null || now.isAfter(existing.startedAt.plus(duration))) {
            return new Window(now, 0);
        }
        return existing;
    }

    private static final class Window {
        private final Instant startedAt;
        private int count;

        private Window(Instant startedAt, int count) {
            this.startedAt = startedAt;
            this.count = count;
        }
    }
}
