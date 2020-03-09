package edu.boun.edgecloudsim.applications.rsu_placement.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;

import static java.lang.Math.*;

public class RandomRSUGenerator {

    private static String nodeTemplate = "\t<datacenter arch=\"x86\" os=\"Linux\" vmm=\"Xen\">\n" +
            "\t\t<costPerBw>0.1</costPerBw>\n" +
            "\t\t<costPerSec>3.0</costPerSec>\n" +
            "\t\t<costPerMem>0.05</costPerMem>\n" +
            "\t\t<costPerStorage>0.1</costPerStorage>\n" +
            "\t\t<location>\n" +
            "\t\t\t<x_pos>%s</x_pos>\n" +
            "\t\t\t<y_pos>%s</y_pos>\n" +
            "\t\t\t<wlan_id>%s</wlan_id>\n" +
            "\t\t\t<attractiveness>0</attractiveness>\n" +
            "\t\t</location>\n" +
            "\t\t<hosts>\n" +
            "\t\t\t<host>\n" +
            "\t\t\t\t<core>2</core>\n" +
            "\t\t\t\t<mips>2500</mips>\n" +
            "\t\t\t\t<ram>1000</ram>\n" +
            "\t\t\t\t<storage>20000</storage>\n" +
            "\t\t\t\t<VMs>\n" +
            "\t\t\t\t\t<VM vmm=\"Xen\">\n" +
            "\t\t\t\t\t\t<core>1</core>\n" +
            "\t\t\t\t\t\t<mips>1250</mips>\n" +
            "\t\t\t\t\t\t<ram>500</ram>\n" +
            "\t\t\t\t\t\t<storage>10000</storage>\n" +
            "\t\t\t\t\t</VM>\n" +
            "\t\t\t\t</VMs>\n" +
            "\t\t\t</host>\n" +
            "\t\t</hosts>\n" +
            "\t</datacenter>";


    public static void main(String[] args) throws IOException {
        String outputFolder = "scripts/rsu_placement/random_rsu/";
        final Path file1 = Paths.get(outputFolder, "random_rsu.xml");
        final Path file2 = Paths.get(outputFolder, "random_rsu_coordinates.csv");

        try (
                final BufferedWriter rsuBW = Files.newBufferedWriter(file1, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                final BufferedWriter csvBW = Files.newBufferedWriter(file2, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        ) {
            appendToFile(rsuBW, "<?xml version=\"1.0\"?>");
            appendToFile(rsuBW, "<edge_devices>");
            appendToFile(csvBW, "title,latitude,longitude");

            Random random = new Random();
            double lat = 51.53183, lang = -0.10631;

            for(int i=0; i<100; i++){
                int bearing = random.nextInt(360);
                int distance = random.nextInt(1500);
                double[] coordinates = movePoint(lat, lang, distance, bearing);

                String node = String.format(nodeTemplate, coordinates[1], coordinates[0], i);
                appendToFile(rsuBW, node);

                String csvLine = String.format("%d,%f,%f", i, coordinates[0], coordinates[1]);
                appendToFile(csvBW, csvLine);
            }
            appendToFile(rsuBW, "</edge_devices>");
        }

    }



    private static double[] movePoint(double latitude, double longitude, double distanceInMetres, double bearing) {
        double brngRad = toRadians(bearing);
        double latRad = toRadians(latitude);
        double lonRad = toRadians(longitude);
        int earthRadiusInMetres = 6371000;
        double distFrac = distanceInMetres / earthRadiusInMetres;

        double latitudeResult = asin(sin(latRad) * cos(distFrac) + cos(latRad) * sin(distFrac) * cos(brngRad));
        double a = atan2(sin(brngRad) * sin(distFrac) * cos(latRad), cos(distFrac) - sin(latRad) * sin(latitudeResult));
        double longitudeResult = (lonRad + a + 3 * PI) % (2 * PI) - PI;


        double lat = toDegrees(latitudeResult);
        BigDecimal latBD = new BigDecimal(Double.toString(lat));
        latBD = latBD.setScale(5, RoundingMode.HALF_UP);

        double lng = toDegrees(longitudeResult);
        BigDecimal lngBD = new BigDecimal(Double.toString(lng));
        lngBD = lngBD.setScale(5, RoundingMode.HALF_UP);

        System.out.println("latitude: " + latBD.doubleValue() + ", longitude: " + lngBD.doubleValue());
        return new double[]{latBD.doubleValue(), lngBD.doubleValue()};
    }

    private static void appendToFile(BufferedWriter bw, String line) throws IOException {
        bw.write(line);
        bw.newLine();
    }
}
