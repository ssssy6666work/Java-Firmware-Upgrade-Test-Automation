package com.renkai.firmware;

import com.renkai.firmware.device.DeviceConfiguration;
import com.renkai.firmware.device.MockDeviceClient;
import com.renkai.firmware.firmware.ChecksumUtil;
import com.renkai.firmware.firmware.FirmwarePackage;
import com.renkai.firmware.firmware.FirmwareValidator;
import com.renkai.firmware.report.MarkdownReportGenerator;
import com.renkai.firmware.upgrade.FirmwareUpgradeService;
import com.renkai.firmware.upgrade.UpgradeResult;
import com.renkai.firmware.validation.LogValidator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class App {
    public static void main(String[] args) throws Exception {
        DeviceConfiguration configuration =
                new DeviceConfiguration("192.168.1.10", 20, "Lab-Switch");

        MockDeviceClient device =
                new MockDeviceClient("SW-1000", "1.0.0", configuration);

        byte[] firmwareData =
                "SW-1000 firmware version 1.1.0".getBytes(StandardCharsets.UTF_8);

        FirmwarePackage firmwarePackage = new FirmwarePackage(
                "SW-1000-v1.1.0.bin",
                "SW-1000",
                "1.1.0",
                firmwareData,
                ChecksumUtil.sha256(firmwareData)
        );

        FirmwareUpgradeService service = new FirmwareUpgradeService(
                new FirmwareValidator(),
                new LogValidator()
        );

        UpgradeResult result = service.upgrade(device, firmwarePackage, 3000, 2);

        new MarkdownReportGenerator().generate(
                result,
                Paths.get("target/reports/firmware-upgrade-report.md")
        );

        System.out.println("Status: " + result.getStatus());
        System.out.println("Message: " + result.getMessage());
    }
}
