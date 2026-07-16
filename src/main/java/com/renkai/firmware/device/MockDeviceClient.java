package com.renkai.firmware.device;

import com.renkai.firmware.firmware.FirmwarePackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MockDeviceClient implements DeviceClient {
    private final String model;
    private String firmwareVersion;
    private DeviceState state = DeviceState.ONLINE;
    private final DeviceConfiguration configuration;
    private final List<String> logs = new ArrayList<String>();

    private FirmwarePackage uploadedFirmware;
    private boolean simulateTimeout;
    private boolean simulateRebootFailure;

    public MockDeviceClient(String model,
                            String firmwareVersion,
                            DeviceConfiguration configuration) {
        this.model = model;
        this.firmwareVersion = firmwareVersion;
        this.configuration = configuration;
        logs.add("INFO Device initialized");
    }

    public void setSimulateTimeout(boolean simulateTimeout) {
        this.simulateTimeout = simulateTimeout;
    }

    public void setSimulateRebootFailure(boolean simulateRebootFailure) {
        this.simulateRebootFailure = simulateRebootFailure;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    @Override
    public DeviceState getState() {
        return state;
    }

    @Override
    public DeviceConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void uploadFirmware(FirmwarePackage firmwarePackage) {
        this.uploadedFirmware = firmwarePackage;
        logs.add("INFO Firmware uploaded: " + firmwarePackage.getVersion());
    }

    @Override
    public void startUpgrade() {
        if (uploadedFirmware == null) {
            state = DeviceState.FAILED;
            logs.add("ERROR No firmware package uploaded");
            return;
        }

        state = DeviceState.UPGRADING;
        logs.add("INFO Upgrade started");

        if (simulateTimeout) {
            logs.add("WARN Device remains in UPGRADING state");
            return;
        }

        state = DeviceState.REBOOTING;
        logs.add("INFO Device rebooting");

        if (simulateRebootFailure) {
            state = DeviceState.FAILED;
            logs.add("BOOT FAILURE Device did not return online");
            return;
        }

        firmwareVersion = uploadedFirmware.getVersion();
        state = DeviceState.ONLINE;
        logs.add("INFO Upgrade completed");
    }

    @Override
    public boolean isOnline() {
        return state == DeviceState.ONLINE;
    }

    @Override
    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }
}
