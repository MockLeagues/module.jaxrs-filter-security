/**
 * Copyright (C) 2013 Guestful (info@guestful.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guestful.jaxrs.security.session;

import org.redisson.Redisson;
import org.redisson.core.RBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class RedissonSessionRepository implements SessionRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedissonSessionRepository.class);
    private static final String PREFIX = "api:sessions:";
    private final Redisson redisson;

    public RedissonSessionRepository(Redisson redisson) {
        this.redisson = redisson;
    }

    @Override
    public void saveSession(String system, StoredSession storedSession) {
        String key = key(system, storedSession.getId());
        LOGGER.trace(storedSession.getPrincipal() + "  Saving session " + storedSession.getId());
        redisson.getBucket(key).set(storedSession, storedSession.getTTL(), TimeUnit.SECONDS);
    }

    @Override
    public void removeSession(String system, String sessionId) {
        String key = key(system, sessionId);
        LOGGER.trace("removeSession() " + sessionId);
        redisson.getBucket(key).delete();
    }

    @Override
    public StoredSession findSession(String system, String sessionId) {
        String key = key(system, sessionId);
        RBucket<StoredSession> bucket = redisson.<StoredSession>getBucket(key);
        try {
            return bucket.get();
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Removing malformed session " + sessionId + ": " + e.getMessage(), e);
            bucket.delete();
            return null;
        }
    }

    @Override
    public Collection<StoredSession> findSessions(String system) {
        String key = key(system, "*");
        return redisson.<StoredSession>getBuckets(key).stream().map(RBucket::get).collect(Collectors.toList());
    }

    private String key(String system, String id) {
        return system == null || system.equals("") ? (PREFIX + id) : (PREFIX + system + ":" + id);
    }

}
