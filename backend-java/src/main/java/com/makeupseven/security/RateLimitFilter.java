package com.makeupseven.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 100;
    private static final long WINDOW_MS = 60_000;
    private final Map<String, Window> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.startsWith("/uploads")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr() + ":" + path.split("/")[Math.min(3, path.split("/").length - 1)];
        Window window = buckets.computeIfAbsent(key, k -> new Window());
        if (!window.tryAcquire()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Try again later.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static class Window {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long resetAt = System.currentTimeMillis() + WINDOW_MS;

        boolean tryAcquire() {
            long now = System.currentTimeMillis();
            if (now > resetAt) {
                count.set(0);
                resetAt = now + WINDOW_MS;
            }
            return count.incrementAndGet() <= MAX_REQUESTS;
        }
    }
}
