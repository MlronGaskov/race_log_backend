package miron.gaskov.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IpRateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final long CAPACITY = 60;
    private static final long REFILL_TOKENS = 60;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = extractClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, this::newBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"message":"Too many requests from this IP. Please try again later."}
                    """);
            log.warn("Rate limit exceeded for IP {}", ip);
        }
    }

    private Bucket newBucket(String ip) {
        Refill refill = Refill.greedy(REFILL_TOKENS, REFILL_PERIOD);
        Bandwidth limit = Bandwidth.classic(CAPACITY, refill);

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

