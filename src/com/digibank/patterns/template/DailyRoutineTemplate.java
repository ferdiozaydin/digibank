package com.digibank.patterns.template;

import com.digibank.patterns.singleton.AuditLogger;

/**
 * Template Method pattern to enforce routine steps.
 */
public abstract class DailyRoutineTemplate {

    // Template method
    public final void runRoutine() {
        preCheckSensors();
        executeRoutine();
        postReport();
    }

    protected void preCheckSensors() {
        AuditLogger.getInstance().log("[Routine] On kontrol adimi tamamlandi.");
    }

    protected abstract void executeRoutine();

    protected void postReport() {
        AuditLogger.getInstance().log("[Routine] Raporlama tamamlandi.");
    }
}
