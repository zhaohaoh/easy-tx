package com.easy.tx.constant;

import java.util.Arrays;
import java.util.StringJoiner;

public class RedisCacheKeyBuilder {
    private static final String REDIS_PREFIX = "EASY_TX:";
    private static final String SPLITE = ":";
    private static final String LOCK_TX = "LOCK_TX:";
    private static final String LOCK_VALUE = "LOCK_VALUE:";

    public static String buildLockValue(String... key) {
        StringJoiner sj = new StringJoiner(SPLITE);
        Arrays.stream(key).forEach(sj::add);
        return REDIS_PREFIX + LOCK_VALUE + sj;
    }

    public static String buildLockTx(String... key) {
        StringJoiner sj = new StringJoiner(SPLITE);
        Arrays.stream(key).forEach(sj::add);
        return REDIS_PREFIX + LOCK_TX + sj;
    }
}
