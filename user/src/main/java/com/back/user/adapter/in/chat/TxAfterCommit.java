package com.back.user.adapter.in.chat;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class TxAfterCommit {

    private TxAfterCommit() {}

    public static void run(Runnable r) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    r.run();
                }
            });
            return;
        }
        r.run();
    }
}
