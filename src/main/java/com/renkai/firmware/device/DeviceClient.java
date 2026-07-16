package com.renkai.firmware.device;

import com.renkai.firmware.firmware.FirmwarePackage;

import java.util.List;

public interface DeviceClient {
    String getModel();
    String getFirmwareVersion();
    DeviceState getState();
    DeviceConfiguration getConfiguration();
    void uploadFirmware(FirmwarePackage firmwarePackage);
    void startUpgrade();
    boolean isOnline();
    List<String> getLogs();
}
