package com.renkai.firmware.device;

import java.util.Objects;

public class DeviceConfiguration {
    private final String ipAddress;
    private final int vlanId;
    private final String deviceName;

    public DeviceConfiguration(String ipAddress, int vlanId, String deviceName) {
        this.ipAddress = ipAddress;
        this.vlanId = vlanId;
        this.deviceName = deviceName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getVlanId() {
        return vlanId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DeviceConfiguration)) return false;
        DeviceConfiguration other = (DeviceConfiguration) obj;
        return vlanId == other.vlanId
                && Objects.equals(ipAddress, other.ipAddress)
                && Objects.equals(deviceName, other.deviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, vlanId, deviceName);
    }

    @Override
    public String toString() {
        return "DeviceConfiguration{" +
                "ipAddress='" + ipAddress + '\'' +
                ", vlanId=" + vlanId +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }
}
