package com.easy.tx.constant;

import java.util.Arrays;
import java.util.StringJoiner;

public class RedisCacheKeyBuilder {
    
    private static final String REDIS_PREFIX = "EASY_TX:";
    
    private static final String BRANCH = "BRANCH:";
    
    private static final String BRANCHS = "BRANCHS";
    
    private static final String GLOBAL = "GLOBAL:";
    
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
    
    public static String buildBranchKey(String globalTxId, String branchTxId) {
        return REDIS_PREFIX + GLOBAL + globalTxId + SPLITE + BRANCH + SPLITE + branchTxId;
    }
    
    public static String buildBranchsKey(String... key) {
        StringJoiner sj = new StringJoiner(SPLITE);
        Arrays.stream(key).forEach(sj::add);
        return REDIS_PREFIX + GLOBAL + sj + SPLITE + BRANCHS;
    }
    
    public static String buildGlobalKey(String... key) {
        StringJoiner sj = new StringJoiner(SPLITE);
        Arrays.stream(key).forEach(sj::add);
        return REDIS_PREFIX + GLOBAL + sj;
    }
    
    public static String buildGlobalStatusKey() {
        return REDIS_PREFIX + GLOBAL + "STATUS";
    }
}
