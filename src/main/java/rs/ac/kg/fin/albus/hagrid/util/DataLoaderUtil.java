package rs.ac.kg.fin.albus.hagrid.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class DataLoaderUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> Map<String, T> loadDataMap(String rootDirPath, Class<T> type) {
        File[] files = new File(rootDirPath).listFiles();

        return Arrays.stream(files)
                .filter(File::isFile)
                .collect(
                        Collectors.toMap(
                                file -> FileUtil.getFilenameWithoutExtension(file.getName()),
                                file -> DataLoaderUtil.readDataFromFile(file, type))
                );
    }

    private static <T> T readDataFromFile(File file, Class<T> returnType) {
        try {
            return OBJECT_MAPPER.readValue(file, returnType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
