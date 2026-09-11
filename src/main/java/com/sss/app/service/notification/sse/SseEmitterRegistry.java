package com.sss.app.service.notification.sse;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// In-memory V1 registry — one JVM, one map. A user can have more than one
// open tab/window, so each user key holds a list of emitters rather than a
// single one; every send fans out to all of them. No Redis here (the spec
// explicitly says not to introduce it yet) — swapping this for a Redis
// Pub/Sub-backed version later only touches sendToUser's fan-out, since
// register/remove/subscribe are already local-connection concerns that a
// multi-instance deploy would keep local regardless.
@Component
@Slf4j
public class SseEmitterRegistry {

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    public void register(Long userSeqp, SseEmitter emitter) {
        emittersByUser.computeIfAbsent(userSeqp, k -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public void remove(Long userSeqp, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByUser.get(userSeqp);
        if (emitters == null) return;
        emitters.remove(emitter);
        // Drop the key entirely once empty — otherwise the map accumulates
        // one empty list per user who has ever connected, for the life of
        // the process.
        emittersByUser.computeIfPresent(userSeqp, (k, list) -> list.isEmpty() ? null : list);
    }

    public void sendToUser(Long userSeqp, String eventName, Object data) {
        List<SseEmitter> emitters = emittersByUser.get(userSeqp);
        if (emitters == null || emitters.isEmpty()) return;
        for (SseEmitter emitter : emitters) {
            send(userSeqp, emitter, eventName, data);
        }
    }

    // Fires on a schedule, not per-heartbeat-on-demand — sends a tiny
    // no-op payload to every open connection so an idle proxy/load-balancer
    // never sees a connection go quiet long enough to time it out. A dead
    // emitter fails the write and is dropped right here, which is also how
    // this registry notices a client that vanished without a clean
    // disconnect (crashed tab, killed network) rather than leaking it.
    @Scheduled(fixedRate = 25_000)
    public void heartbeat() {
        if (emittersByUser.isEmpty()) return;
        emittersByUser.forEach((userSeqp, emitters) -> {
            for (SseEmitter emitter : emitters) {
                send(userSeqp, emitter, "heartbeat", Map.of());
            }
        });
    }

    private void send(Long userSeqp, SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            // Client gone (closed tab, network drop, proxy timeout) — drop it
            // here rather than waiting for onError/onCompletion to fire, so a
            // send failure never gets retried against the same dead emitter.
            remove(userSeqp, emitter);
            try {
                emitter.completeWithError(e);
            } catch (Exception ignored) {
                // Already broken; nothing left to clean up.
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        emittersByUser.values().forEach(list -> list.forEach(SseEmitter::complete));
        emittersByUser.clear();
    }
}
