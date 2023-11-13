//package com.easy.tx.context;
//
//import javafx.util.Pair;
//
//import java.util.LinkedList;
//
///**
// * 分支事务上下文
// *
// * @author hzh
// * @date 2023/08/01
// */
//public class BranchTxContext {
//
//    private static final ThreadLocal<Pair<String /*/globalTxId/*/ , LinkedList<String>>> LOCAL_TX_ID_CONTEXT = new ThreadLocal<>();
//
//
//    /**
//     * 获取第一个事务id
//     *
//     * @return {@link String}
//     */
//    public static String getFirstTxId() {
//        Pair<String, LinkedList<String>> pair = LOCAL_TX_ID_CONTEXT.get();
//        if (pair == null) {
//            return null;
//        }
//        return pair.getValue().getFirst();
//    }
//
//    /**
//     * 获取最后事务id
//     *
//     * @return {@link String}
//     */
//    public static String peekLastTxId() {
//        Pair<String, LinkedList<String>> pair = LOCAL_TX_ID_CONTEXT.get();
//        if (pair == null) {
//            return null;
//        }
//        return pair.getValue().peekLast();
//    }
//
//    public static LinkedList<String> get() {
//        Pair<String, LinkedList<String>> pair = LOCAL_TX_ID_CONTEXT.get();
//        if (pair == null) {
//            return new LinkedList<>();
//        }
//        return pair.getValue();
//    }
//
//    public static String removeLast() {
//        Pair<String, LinkedList<String>> pair = LOCAL_TX_ID_CONTEXT.get();
//        if (pair == null) {
//            return null;
//        }
//        return pair.getValue().removeLast();
//    }
//
//    /**
//     * 保存
//     *
//     * @param txId 事务id
//     */
//    public static void add(String globalTxId, String txId) {
//        Pair<String, LinkedList<String>> pair = LOCAL_TX_ID_CONTEXT.get();
//        if (pair == null) {
//            LOCAL_TX_ID_CONTEXT.set(new Pair<>(globalTxId, new LinkedList<>()));
//            pair = LOCAL_TX_ID_CONTEXT.get();
//        }
//        pair.getValue().addLast(txId);
//    }
//
//
//    /**
//     * 删除事务id
//     *
//     * @param txId 事务id
//     */
//    public static void removeTxId(String txId) {
//        Pair<String, LinkedList<String>> pair = LOCAL_TX_ID_CONTEXT.get();
//        if (pair == null) {
//            return;
//        }
//        pair.getValue().remove(txId);
//    }
//
//    /**
//     * 清空
//     */
//    public static void clear() {
//        LOCAL_TX_ID_CONTEXT.remove();
//    }
//}
