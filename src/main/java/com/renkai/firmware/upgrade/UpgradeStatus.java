package com.renkai.firmware.upgrade;

public enum UpgradeStatus {
    PASSED,
    VALIDATION_FAILED,
    TIMEOUT,
    REBOOT_FAILED,
    VERSION_MISMATCH,
    CONFIGURATION_CHANGED,
    LOG_ERROR
}
