package rs.ac.kg.fin.albus.hagrid.code;

import org.springframework.stereotype.Component;
import org.testcontainers.shaded.com.google.common.cache.Cache;
import org.testcontainers.shaded.com.google.common.cache.CacheBuilder;
import rs.ac.kg.fin.albus.hagrid.config.CodeTemplatesParametersConfig;
import rs.ac.kg.fin.albus.hagrid.exception.HagridException;
import rs.ac.kg.fin.albus.hagrid.util.FileUtil;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class CodeFileHandler {

    private static final String CODE_MARKER = "@@@@@@CODE@@@@@@";

    private final String codeTemplatesRootDirPath;

    // TODO: make it configurable
    private final Cache<String, Optional<String>> codeTemplatesCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.of(3, ChronoUnit.HOURS))
            .build();

    public CodeFileHandler(CodeTemplatesParametersConfig codeTemplatesParametersConfig) {
        this.codeTemplatesRootDirPath = codeTemplatesParametersConfig.getDirPath();
    }

    public Path createFileWithContent(String rootDir,
                                      String environment,
                                      String assignmentId,
                                      String submissionId,
                                      String extension,
                                      String code) {
        String dirPath = String.format("%s/%s", rootDir, assignmentId);
        String filename = String.format("%s.%s", submissionId, extension);
        File directory = FileUtil.createDirIfNotExists(dirPath);
        return FileUtil.createFile(directory, filename, mergeCodeWithTemplate(environment, code));
    }

    private String mergeCodeWithTemplate(String environment, String code) {
        try {
            Optional<String> content = codeTemplatesCache.get(environment, () -> getCodeTemplate(environment));
            if (content.isEmpty()) {
                return code;
            }

            return content.get().replace(CODE_MARKER, code);
        } catch (ExecutionException e) {
            throw new HagridException(
                    String.format("Could not get the code template for %s", environment), e
            );
        }

    }

    private Optional<String> getCodeTemplate(String environment) {
        return Optional.ofNullable(FileUtil.getFileContentSilently(
                String.format("%s/%s/", codeTemplatesRootDirPath, environment)
        ));
    }
}
