package dio.budgeting.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiRateLimitService {

    private static final int REQUEST_LIMIT = 10;

    private final Map<UUID, Bucket> buckets =
            new ConcurrentHashMap<>();

    public boolean tryConsume(UUID userId) {

        Bucket bucket =
                buckets.computeIfAbsent(
                        userId,
                        id -> createBucket()
                );

        return bucket.tryConsume(1);
    }

    private Bucket createBucket() {

        Refill refill =
                Refill.intervally(
                        REQUEST_LIMIT,
                        Duration.ofMinutes(1)
                );

        Bandwidth limit =
                Bandwidth.classic(
                        REQUEST_LIMIT,
                        refill
                );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}