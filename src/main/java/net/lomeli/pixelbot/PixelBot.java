package net.lomeli.pixelbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import okhttp3.OkHttpClient;

import java.io.*;
import java.util.List;

public class PixelBot {
    private static PrintStream print = System.out;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String MASTODON_INSTANCE = "mastodon.cloud";
    private static String LAST_OTA_CHECK_FILE = "latestversioncheck.json";
    private static Statuses statuses;
    private static List<String> deviceIDs;
    private static Device[] oldDeviceOTA, latestDeviceOTA;
    private static boolean firstRead = true;

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 1) {
            print.println("Please provide a access token as an argument. Exiting...");
            System.exit(0);
        }
        print.println("Starting Pixel OTA Bot...");
        String accessToken = args[0];
        print.println("Logging into mastodon.cloud");
        MastodonClient client = new MastodonClient.Builder(MASTODON_INSTANCE, new OkHttpClient.Builder(), gson)
                .accessToken(accessToken).build();
        statuses = new Statuses(client);

        deviceIDs = OTAScrapper.getDeviceIDs();
        if (deviceIDs.size() < 1) {
            print.println("No devices to lookup. Exiting...");
            System.exit(0);
        }
        readLastOTAInfo();

        while (true) {
            getLatestDeviceInfo();
            if (oldDeviceOTA == null) {
                for (Device device : latestDeviceOTA) {
                    postDeviceUpdate(device);
                }
            } else {
                for (int i = 0; i < latestDeviceOTA.length; i++) {
                    Device oldDeviceInfo = oldDeviceOTA[i];
                    Device latestDeviceInfo = latestDeviceOTA[i];
                    if (!latestDeviceInfo.getLatestOTA().doHashesMatch(oldDeviceInfo.getLatestOTA()))
                        postDeviceUpdate(latestDeviceInfo);
                }
            }
            Thread.sleep(1000 * 60 * 60);
        }
    }

    private static void postDeviceUpdate(Device device) {
        StringBuilder builder = new StringBuilder();
        builder.append("Latest OTA for ").append(device.getDeviceName()).append(" (ID: ").append(device.getDeviceID()).append(")\n\n");
        builder.append("OTA Description: ").append(device.getLatestOTA().getDescription()).append("\n\n");
        builder.append("SHA-256 Checksum: ").append(device.getLatestOTA().getHash()).append("\n\n");
        builder.append("Download: ").append(device.getLatestOTA().getDownloadLink()).append("\n");
        builder.append("#").append(device.getDeviceName().toLowerCase().replaceAll("\\s+", ""));
        builder.append(" #").append(device.getDeviceID()).append(" #ota");

        try {
            statuses.postStatus(builder.toString(), null,
                    null, false, null, Status.Visibility.Public).execute();
        } catch (Mastodon4jRequestException ex) {
            print.println("Failed to post OTA update info.");
            ex.printStackTrace();
        }
    }

    private static void getLatestDeviceInfo() {
        print.println("Getting latest device info.");
        if (!firstRead && latestDeviceOTA != null)
            oldDeviceOTA = latestDeviceOTA;
        else firstRead = false;
        latestDeviceOTA = OTAScrapper.getDeviceInfoList(deviceIDs);
        writeLastOTAInfo();
    }

    private static void readLastOTAInfo() {
        print.println("Attempting to read last OTA info recorded.");
        File lastVersionChecked = new File(LAST_OTA_CHECK_FILE);
        if (lastVersionChecked.exists() && lastVersionChecked.isFile()) {
            try {
                oldDeviceOTA = gson.fromJson(new JsonReader(new FileReader(lastVersionChecked.getAbsolutePath())),
                        Device[].class);
            } catch (FileNotFoundException ex) {
                print.println("How the hell did we get here?");
                ex.printStackTrace();
            }
        }
    }

    private static void writeLastOTAInfo() {
        if (oldDeviceOTA != null) {
            print.println("Writing latest OTA info for future reference.");
            File lastVersionChecked = new File(LAST_OTA_CHECK_FILE);
            if (lastVersionChecked.exists() && lastVersionChecked.isFile())
                lastVersionChecked.delete();

            String text = gson.toJson(oldDeviceOTA);
            try {
                FileWriter writer = new FileWriter(lastVersionChecked);
                writer.write(text);
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
