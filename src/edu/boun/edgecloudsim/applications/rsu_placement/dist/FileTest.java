package edu.boun.edgecloudsim.applications.rsu_placement.dist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class FileTest {

    public static void main(String[] args) throws IOException {

        String filePath = "scripts/rsu_placement/output-app";
        long count = Files.walk(Paths.get(filePath))
                .map(Path::toFile)
                .filter(e -> e.isFile())
                .filter(e -> !e.getName().startsWith("SIM"))
                .flatMap(e -> {
                    try {
                        return Files.lines(e.toPath());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        return Stream.empty();
                    }
                })
                .filter(s -> !s.startsWith("-"))
                .count();

        System.out.println(count);
    }
}
