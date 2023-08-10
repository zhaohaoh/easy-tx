package com.easy.tx.context;

import javafx.util.Pair;
import org.springframework.util.StringUtils;


/**
 * 全局事务上下文
 *
 * @author hzh
 * @date 2023/08/01
 */
public class GlobalTxContext {

    /**
     * 上下文持有人
     */
    private static final ThreadLocal<Pair<String, Long>> TX_SESSION = new ThreadLocal<>();


    /**
     * 获取全局事务id
     *
     * @return {@link String}
     */
    public static String getGlobalTxId() {
        Pair<String, Long> txSession = TX_SESSION.get();
        if (txSession != null) {
            String globalTxId = txSession.getKey();
            if (!StringUtils.isEmpty(globalTxId)) {
                return globalTxId;
            }
        }
        return null;
    }

    /**
     * 获取到期时间
     *
     * @return {@link Long}
     */
    public static Long getExpireTime() {
        Pair<String, Long> txSession = TX_SESSION.get();
        if (txSession != null) {
            return txSession.getValue();
        }
        return null;
    }

    /**
     * 绑定
     *
     * @param globalTxId 全局事务id
     * @param expireTime 到期时间
     * @return {@link String}
     */
    public static String bind(String globalTxId, Long expireTime) {
        TX_SESSION.set(new Pair<>(globalTxId, expireTime));
        return globalTxId;
    }


    public static void remove() {
        TX_SESSION.remove();
    }


}
