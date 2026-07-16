package com.renkai.firmware.firmware;

public class FirmwareValidator {

    public void validate(FirmwarePackage firmwarePackage, String deviceModel) {
        if (firmwarePackage == null) {
            throw new IllegalArgumentException("Firmware package cannot be null");
        }

        if (!firmwarePackage.getFileName().endsWith(".bin")) {
            throw new IllegalArgumentException("Firmware file must use .bin extension");
        }

        if (!deviceModel.equals(firmwarePackage.getTargetModel())) {
            throw new IllegalArgumentException(
                    "Firmware model mismatch: expected " + deviceModel
                            + ", actual " + firmwarePackage.getTargetModel());
        }

        String actualChecksum = ChecksumUtil.sha256(firmwarePackage.getContent());
        if (!actualChecksum.equalsIgnoreCase(firmwarePackage.getExpectedChecksum())) {
            throw new IllegalArgumentException("Firmware checksum validation failed");
        }
    }
}
