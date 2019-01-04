package net.lomeli.pixelbot;

public class DeviceOTA {
    private String description;
    private String downloadLink;
    private String hash;

    public DeviceOTA(String description, String downloadLink, String hash) {
        this.description = description;
        this.downloadLink = downloadLink;
        this.hash = hash;
    }

    public String getDescription() {
        return description;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "otaVersion: " + getDescription() + ", " +
                "otaDownload: " + getDownloadLink() + ", " +
                "hash: " + getHash();
    }

    public boolean doHashesMatch(DeviceOTA device) {
        return getHash().equalsIgnoreCase(device.getHash());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceOTA) return doHashesMatch((DeviceOTA) obj);
        return super.equals(obj);
    }
}
