package com.renkai.firmware.upgrade;

import com.renkai.firmware.device.DeviceClient;
import com.renkai.firmware.device.DeviceConfiguration;
import com.renkai.firmware.device.DeviceState;
import com.renkai.firmware.firmware.FirmwarePackage;
import com.renkai.firmware.firmware.FirmwareValidator;
import com.renkai.firmware.validation.LogValidator;
import com.renkai.firmware.validation.ValidationResult;

public class FirmwareUpgradeService {
    private final FirmwareValidator firmwareValidator;
    private final LogValidator logValidator;

    public FirmwareUpgradeService(FirmwareValidator firmwareValidator,
                                  LogValidator logValidator) {
        this.firmwareValidator = firmwareValidator;
        this.logValidator = logValidator;
    }

    public UpgradeResult upgrade(DeviceClient device,
                                 FirmwarePackage firmwarePackage,
                                 long timeoutMs,
                                 int maxAttempts) {
        long startTime = System.currentTimeMillis();
        String oldVersion = device.getFirmwareVersion();
        DeviceConfiguration oldConfiguration = device.getConfiguration();

        try {
            firmwareValidator.validate(firmwarePackage, device.getModel());
        } catch (IllegalArgumentException e) {
            return result(UpgradeStatus.VALIDATION_FAILED, oldVersion,
                    firmwarePackage.getVersion(), 0, startTime, e.getMessage());
        }

        int attempts = 0;
        while (attempts < maxAttempts) {
            attempts++;
            device.uploadFirmware(firmwarePackage);
            device.startUpgrade();

            if (device.getState() == DeviceState.UPGRADING) {
                if (System.currentTimeMillis() - startTime >= timeoutMs || timeoutMs == 0) {
                    return result(UpgradeStatus.TIMEOUT, oldVersion,
                            firmwarePackage.getVersion(), attempts, startTime,
                            "Upgrade exceeded timeout");
                }
                continue;
            }

            if (!device.isOnline()) {
                if (attempts >= maxAttempts) {
                    return result(UpgradeStatus.REBOOT_FAILED, oldVersion,
                            firmwarePackage.getVersion(), attempts, startTime,
                            "Device did not return online");
                }
                continue;
            }

            if (!firmwarePackage.getVersion().equals(device.getFirmwareVersion())) {
                return result(UpgradeStatus.VERSION_MISMATCH, oldVersion,
                        firmwarePackage.getVersion(), attempts, startTime,
                        "Device firmware version does not match target version");
            }

            if (!oldConfiguration.equals(device.getConfiguration())) {
                return result(UpgradeStatus.CONFIGURATION_CHANGED, oldVersion,
                        firmwarePackage.getVersion(), attempts, startTime,
                        "Device configuration changed after upgrade");
            }

            ValidationResult logResult = logValidator.validate(device.getLogs());
            if (!logResult.isPassed()) {
                return result(UpgradeStatus.LOG_ERROR, oldVersion,
                        firmwarePackage.getVersion(), attempts, startTime,
                        logResult.getMessage());
            }

            return result(UpgradeStatus.PASSED, oldVersion,
                    firmwarePackage.getVersion(), attempts, startTime,
                    "Firmware upgrade completed successfully");
        }

        return result(UpgradeStatus.REBOOT_FAILED, oldVersion,
                firmwarePackage.getVersion(), attempts, startTime,
                "Upgrade failed");
    }

    private UpgradeResult result(UpgradeStatus status,
                                 String oldVersion,
                                 String targetVersion,
                                 int attempts,
                                 long startTime,
                                 String message) {
        return new UpgradeResult(
                status,
                oldVersion,
                targetVersion,
                attempts,
                System.currentTimeMillis() - startTime,
                message
        );
    }
}
