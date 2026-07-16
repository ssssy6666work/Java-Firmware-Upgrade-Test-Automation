package com.renkai.firmware;

import com.renkai.firmware.device.DeviceConfiguration;
import com.renkai.firmware.device.MockDeviceClient;
import com.renkai.firmware.firmware.ChecksumUtil;
import com.renkai.firmware.firmware.FirmwarePackage;
import com.renkai.firmware.firmware.FirmwareValidator;
import com.renkai.firmware.upgrade.FirmwareUpgradeService;
import com.renkai.firmware.upgrade.UpgradeResult;
import com.renkai.firmware.upgrade.UpgradeStatus;
import com.renkai.firmware.validation.LogValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirmwareUpgradeServiceTest {
    private FirmwareUpgradeService service;
    private DeviceConfiguration configuration;

    @BeforeEach
    void setUp() {
        service = new FirmwareUpgradeService(
                new FirmwareValidator(),
                new LogValidator()
        );
        configuration =
                new DeviceConfiguration("192.168.1.10", 20, "Lab-Switch");
    }

    @Test
    void shouldUpgradeFirmwareSuccessfully() {
        MockDeviceClient device =
                new MockDeviceClient("SW-1000", "1.0.0", configuration);

        FirmwarePackage firmware = validFirmware("SW-1000", "1.1.0");

        UpgradeResult result = service.upgrade(device, firmware, 3000, 2);

        printResult(
                "Successful firmware upgrade",
                UpgradeStatus.PASSED,
                result,
                device
        );

        assertTrue(result.isPassed());
        assertEquals("1.1.0", device.getFirmwareVersion());
        assertEquals(UpgradeStatus.PASSED, result.getStatus());
    }

    @Test
    void shouldRejectCorruptedFirmware() {
        MockDeviceClient device =
                new MockDeviceClient("SW-1000", "1.0.0", configuration);

        byte[] data = "corrupted firmware".getBytes(StandardCharsets.UTF_8);
        FirmwarePackage firmware = new FirmwarePackage(
                "SW-1000-v1.1.0.bin",
                "SW-1000",
                "1.1.0",
                data,
                "incorrect-checksum"
        );

        UpgradeResult result = service.upgrade(device, firmware, 3000, 2);

        printResult(
                "Reject corrupted firmware checksum",
                UpgradeStatus.VALIDATION_FAILED,
                result,
                device
        );

        assertEquals(UpgradeStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("1.0.0", device.getFirmwareVersion());
    }

    @Test
    void shouldRejectFirmwareForDifferentModel() {
        MockDeviceClient device =
                new MockDeviceClient("SW-1000", "1.0.0", configuration);

        FirmwarePackage firmware = validFirmware("AP-2000", "1.1.0");

        UpgradeResult result = service.upgrade(device, firmware, 3000, 2);

        printResult(
                "Reject firmware for a different device model",
                UpgradeStatus.VALIDATION_FAILED,
                result,
                device
        );

        assertEquals(UpgradeStatus.VALIDATION_FAILED, result.getStatus());
    }

    @Test
    void shouldReportTimeout() {
        MockDeviceClient device =
                new MockDeviceClient("SW-1000", "1.0.0", configuration);
        device.setSimulateTimeout(true);

        FirmwarePackage firmware = validFirmware("SW-1000", "1.1.0");

        UpgradeResult result = service.upgrade(device, firmware, 0, 1);

        printResult(
                "Firmware upgrade timeout",
                UpgradeStatus.TIMEOUT,
                result,
                device
        );

        assertEquals(UpgradeStatus.TIMEOUT, result.getStatus());
    }

    @Test
    void shouldReportRebootFailure() {
        MockDeviceClient device =
                new MockDeviceClient("SW-1000", "1.0.0", configuration);
        device.setSimulateRebootFailure(true);

        FirmwarePackage firmware = validFirmware("SW-1000", "1.1.0");

        UpgradeResult result = service.upgrade(device, firmware, 3000, 2);

        printResult(
                "Device reboot failure after firmware upgrade",
                UpgradeStatus.REBOOT_FAILED,
                result,
                device
        );

        assertEquals(UpgradeStatus.REBOOT_FAILED, result.getStatus());
        assertEquals(2, result.getAttempts());
    }

    @Test
    void shouldRejectWrongFileExtension() {
        MockDeviceClient device =
                new MockDeviceClient("SW-1000", "1.0.0", configuration);

        byte[] data =
                "SW-1000 firmware version 1.1.0"
                        .getBytes(StandardCharsets.UTF_8);

        FirmwarePackage firmware = new FirmwarePackage(
                "SW-1000-v1.1.0.txt",
                "SW-1000",
                "1.1.0",
                data,
                ChecksumUtil.sha256(data)
        );

        UpgradeResult result = service.upgrade(device, firmware, 3000, 2);

        printResult(
                "Reject a firmware file without the .bin extension",
                UpgradeStatus.VALIDATION_FAILED,
                result,
                device
        );

        assertEquals(UpgradeStatus.VALIDATION_FAILED, result.getStatus());
        assertEquals("1.0.0", device.getFirmwareVersion());
    }

    /**
     * Prints every test result directly in CMD so beginners can clearly see
     * the expected status, actual status, device version, attempts and logs.
     */
    private void printResult(String testName,
                             UpgradeStatus expectedStatus,
                             UpgradeResult result,
                             MockDeviceClient device) {
        boolean matched = expectedStatus == result.getStatus();

        System.out.println();
        System.out.println("============================================================");
        System.out.println("TEST CASE       : " + testName);
        System.out.println("EXPECTED STATUS : " + expectedStatus);
        System.out.println("ACTUAL STATUS   : " + result.getStatus());
        System.out.println("TEST VERDICT    : " + (matched ? "PASS" : "FAIL"));
        System.out.println("OLD VERSION     : " + result.getOldVersion());
        System.out.println("TARGET VERSION  : " + result.getTargetVersion());
        System.out.println("DEVICE VERSION  : " + device.getFirmwareVersion());
        System.out.println("ATTEMPTS        : " + result.getAttempts());
        System.out.println("DURATION        : " + result.getDurationMs() + " ms");
        System.out.println("MESSAGE         : " + result.getMessage());
        System.out.println("DEVICE STATE    : " + device.getState());
        System.out.println("DEVICE LOGS     :");

        if (device.getLogs().isEmpty()) {
            System.out.println("  (no device log)");
        } else {
            for (String log : device.getLogs()) {
                System.out.println("  - " + log);
            }
        }

        System.out.println("============================================================");
        System.out.println();
    }

    private FirmwarePackage validFirmware(String model, String version) {
        byte[] data =
                (model + " firmware version " + version)
                        .getBytes(StandardCharsets.UTF_8);

        return new FirmwarePackage(
                model + "-v" + version + ".bin",
                model,
                version,
                data,
                ChecksumUtil.sha256(data)
        );
    }
}
