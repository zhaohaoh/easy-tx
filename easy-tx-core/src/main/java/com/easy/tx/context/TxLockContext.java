//package com.easy.tx.context;
//
//import javafx.util.Pair;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 当地事务上下文
// *
// * @author hzh
// * @date 2023/08/01
// */
//public class TxLockContext {
//
//    private static final ThreadLocal<Pair<String, ArrayList<String>>> TX_LOCK_CONTEXT = new ThreadLocal<>();
//
//
//    public static List<String> get() {
//        Pair<String, ArrayList<String>> pair = TX_LOCK_CONTEXT.get();
//        if (pair == null) {
//            return new ArrayList<>();
//        }
//        return pair.getValue();
//    }
//
//
//    /**
//     * 保存
//     */
//    public static void add(String globalTxId, String lockKey) {
//        Pair<String, ArrayList<String>> pair = TX_LOCK_CONTEXT.get();
//        if (pair == null) {
//            TX_LOCK_CONTEXT.set(new Pair<>(globalTxId, new ArrayList<>()));
//            pair = TX_LOCK_CONTEXT.get();
//        }
//        pair.getValue().add(lockKey);
//    }
//
//
//    /**
//     * 清空
//     */
//    public static void clear() {
//        TX_LOCK_CONTEXT.remove();
//    }
//}
