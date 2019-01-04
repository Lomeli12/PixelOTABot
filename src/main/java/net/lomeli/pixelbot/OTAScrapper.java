package net.lomeli.pixelbot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OTAScrapper {
    private static final String OTA_PAGE_URL = "https://developers.google.com/android/ota";

    public static Device[] getDeviceInfoList(List<String> deviceIDs) {
        Device[] devices = new Device[deviceIDs.size()];
        Document document = getOTAPage();
        if (document != null) {
            for (int i = 0; i < deviceIDs.size(); i++) {
                devices[i] = getDeviceOTAs(document, deviceIDs.get(i));
            }
        }
        return devices;
    }

    public static List<String> getDeviceIDs() {
        System.out.println("Reading device ID list.");
        List<String> ids = new ArrayList<>();
        File idFile = new File("deviceIDs.txt");
        if (idFile.exists() && idFile.isFile()) {
            try {
                ids.addAll(Files.readAllLines(Paths.get(idFile.getAbsolutePath())));
            } catch (IOException ex) {
                System.out.println("Failed to get device IDs.");
                ex.printStackTrace();
            }
        }
        return ids;
    }

    private static Device getDeviceOTAs(Document parentDoc, String deviceID) {
        Device device = new Device(deviceID);
        Element deviceHeading = parentDoc.getElementById(deviceID);
        if (deviceHeading != null) {
            String baseString = "\"" + deviceID + "\" for ";
            String deviceName = deviceHeading.text().substring(baseString.length());
            device.setDeviceName(deviceName);

            int index = parentDoc.getAllElements().indexOf(deviceHeading);

            if (index + 1 < parentDoc.getAllElements().size()) {
                Element possibleOTATable = parentDoc.getAllElements().get(index + 1);
                if (possibleOTATable != null && possibleOTATable.tagName().equalsIgnoreCase("table"))
                    readOTATable(device, possibleOTATable);
            }
        }
        return device;
    }

    private static void readOTATable(Device device, Element tableElement) {
        Elements tables = tableElement.getElementsByTag("tbody");
        if (tables != null && tables.size() > 0) {
            Element mainTable = tables.first();
            Elements tableEntries = mainTable.children();
            if (tableEntries != null && tableEntries.size() > 0) {
                Element latestEntry = tableEntries.last();
                if (latestEntry.children().size() > 2) {
                    Element linkTableItem = latestEntry.children().get(1);
                    if (linkTableItem != null && linkTableItem.tagName().equalsIgnoreCase("td")) {
                        if (linkTableItem.children().size() > 0 &&
                                linkTableItem.children().get(0).tagName().equalsIgnoreCase("a")) {
                            String otaName = latestEntry.children().get(0).text();
                            String hash = latestEntry.children().get(2).text();
                            String otaLink = linkTableItem.children().get(0).attr("href");

                            device.setLatestOTA(new DeviceOTA(otaName, otaLink, hash));
                        }
                    }
                }
            }
        }
    }

    private static Document getOTAPage() {
        Document doc = null;
        try {
            doc = Jsoup.connect(OTA_PAGE_URL).get();
        } catch (IOException ex) {
            System.out.println("Failed to load Google OTA page!");
            ex.printStackTrace();
        }
        return doc;
    }
}
