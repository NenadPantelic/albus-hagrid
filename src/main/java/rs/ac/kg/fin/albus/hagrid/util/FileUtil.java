package rs.ac.kg.fin.albus.hagrid.util;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import rs.ac.kg.fin.albus.hagrid.exception.HagridException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FileUtil {

    public static File createDirIfNotExists(String path) {
        File directory = new File(path);

        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                log.error("Could not create directory for {}", path);
                throw new HagridException(String.format("Could not create directory %s", path));
            } else {
                log.info(String.format("Directory %s has been successfully created", path));
            }
        } else {
            log.info(String.format("Directory %s already exists", path));
        }

        return directory;
    }

    public static Path createFile(File parent, String name, String content) {
        File file = new File(parent, name);
        String filepath = String.format("%s/%s", parent, name);

        try {
            if (file.createNewFile()) {
                return Files.writeString(file.toPath(), content, StandardOpenOption.APPEND);
            } else {
                throw new HagridException(String.format("Could not create file %s", filepath));
            }
        } catch (IOException e) {
            log.error("File {} creation failed due to {}", filepath, e.getMessage(), e);
            throw new HagridException(String.format("Could not create file %s", filepath));
        }
    }

    public static void deleteFile(File file) {
        if (file.delete()) {
            log.info("File {} successfully deleted.", file);
        } else {
            log.warn("File {} has not been deleted.", file);
        }
    }

    public static String getFilenameWithoutExtension(String filename) {
        String[] filenameParts = filename.split("\\.");
        if (filenameParts.length == 2) {
            return filenameParts[0];
        }

        throw new IllegalArgumentException("Invalid filename provided, extension is missing");
    }

    public static String getFileContentSilently(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        try {
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new HagridException(
                    String.format("Unable to create file with code due to %s", e.getMessage())
            );
        }
    }
}
