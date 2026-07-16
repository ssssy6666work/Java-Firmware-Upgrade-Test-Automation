package com.renkai.firmware.upgrade;

public class UpgradeResult {
    private final UpgradeStatus status;
    private final String oldVersion;
    private final String targetVersion;
    private final int attempts;
    private final long durationMs;
    private final String message;

    public UpgradeResult(UpgradeStatus status,
                         String oldVersion,
                         String targetVersion,
                         int attempts,
                         long durationMs,
                         String message) {
        this.status = status;
        this.oldVersion = oldVersion;
        this.targetVersion = targetVersion;
        this.attempts = attempts;
        this.durationMs = durationMs;
        this.message = message;
    }

    public UpgradeStatus getStatus() {
        return status;
    }

    public String getOldVersion() {
        return oldVersion;
    }

    public String getTargetVersion() {
        return targetVersion;
    }

    public int getAttempts() {
        return attempts;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPassed() {
        return status == UpgradeStatus.PASSED;
    }
}
