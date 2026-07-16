package com.renkai.firmware.firmware;

public class FirmwarePackage {
    private final String fileName;
    private final String targetModel;
    private final String version;
    private final byte[] content;
    private final String expectedChecksum;

    public FirmwarePackage(String fileName,
                           String targetModel,
                           String version,
                           byte[] content,
                           String expectedChecksum) {
        this.fileName = fileName;
        this.targetModel = targetModel;
        this.version = version;
        this.content = content.clone();
        this.expectedChecksum = expectedChecksum;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTargetModel() {
        return targetModel;
    }

    public String getVersion() {
        return version;
    }

    public byte[] getContent() {
        return content.clone();
    }

    public String getExpectedChecksum() {
        return expectedChecksum;
    }
}
