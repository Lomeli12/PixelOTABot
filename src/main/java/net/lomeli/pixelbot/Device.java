package net.lomeli.pixelbot;

public class Device {
    private String deviceID;
    private String deviceName;
    private DeviceOTA latestOTA;

    public Device(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public DeviceOTA getLatestOTA() {
        return latestOTA;
    }

    public void setLatestOTA(DeviceOTA latestOTA) {
        this.latestOTA = latestOTA;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Device: ").append(getDeviceName()).append("\n");
        builder.append("Device ID: ").append(getDeviceID()).append("\n");
        builder.append("Latest OTA:\n");
        builder.append(getLatestOTA().toString());
        return builder.toString();
    }
}
