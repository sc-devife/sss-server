package com.sss.app.util;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

public final class IdGenerator {

    private IdGenerator() {
    }

    // UUIDv7 (time-ordered): keeps external-facing `uid` columns opaque while
    // avoiding the index fragmentation random (v4) UUIDs cause on insert.
    public static UUID newUid() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
