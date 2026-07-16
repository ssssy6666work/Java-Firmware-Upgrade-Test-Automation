package com.renkai.firmware.report;

import com.renkai.firmware.upgrade.UpgradeResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MarkdownReportGenerator {

    public void generate(UpgradeResult result, Path outputPath) throws IOException {
        String report =
                "# Firmware Upgrade Test Report\n\n" +
                "| Item | Result |\n" +
                "|---|---|\n" +
                "| Status | " + result.getStatus() + " |\n" +
                "| Old version | " + result.getOldVersion() + " |\n" +
                "| Target version | " + result.getTargetVersion() + " |\n" +
                "| Attempts | " + result.getAttempts() + " |\n" +
                "| Duration | " + result.getDurationMs() + " ms |\n" +
                "| Message | " + result.getMessage() + " |\n";

        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        Files.write(outputPath, report.getBytes(StandardCharsets.UTF_8));
    }
}
