/**
 * Copyright (C) 2013 Guestful (info@guestful.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guestful.jaxrs.security.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@Singleton
public class MemorySessionRepository implements SessionRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemorySessionRepository.class);
    private static final String PREFIX = "api:sessions:";

    private final ConcurrentMap<String, StoredSession> sessions = new ConcurrentHashMap<>();
    private Thread scavenger;

    @PostConstruct
    public void init() {
        if (scavenger == null) {
            scavenger = new Thread(MemorySessionRepository.class.getSimpleName() + "-Scavenger") {
                @Override
                public void run() {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            Thread.sleep(60000);
                            sessions.entrySet().stream().filter(e -> e.getValue().isExpired()).forEach(entry -> {
                                LOGGER.trace("scavenger() {}", entry.getKey());
                                sessions.remove(entry.getKey(), entry.getValue());
                            });
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        scavenger = null;
                    }
                }
            };
            LOGGER.trace("init() scavenger thread {}", scavenger.getName());
            scavenger.start();
        }
    }

    @PreDestroy
    public void close() {
        if (scavenger != null) {
            scavenger.interrupt();
            scavenger = null;
        }
    }

    @Override
    public void saveSession(String system, StoredSession storedSession) {
        String key = key(system, storedSession.getId());
        LOGGER.trace("saveSession() {}={}", key, storedSession);
        sessions.put(key, storedSession);
    }

    @Override
    public void removeSession(String system, String sessionId) {
        String key = key(system, sessionId);
        LOGGER.trace("removeSession() {}", key);
        sessions.remove(key);
    }

    @Override
    public StoredSession findSession(String system, String sessionId) {
        String key = key(system, sessionId);
        LOGGER.trace("findSession() {}", key);
        return sessions.get(key);
    }

    @Override
    public Stream<StoredSession> findSessions(String system) {
        String key = key(system, "");
        LOGGER.trace("findSessions() {}", key);
        return sessions.entrySet().stream().filter(e -> e.getKey().startsWith(key)).map(Map.Entry::getValue);
    }

    private String key(String system, String id) {
        return system == null || system.equals("") ? (PREFIX + id) : (PREFIX + system + ":" + id);
    }

}
