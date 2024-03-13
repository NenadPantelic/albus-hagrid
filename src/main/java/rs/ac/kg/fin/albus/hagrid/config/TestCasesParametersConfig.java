package rs.ac.kg.fin.albus.hagrid.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Data
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "test-cases-parameters")
public class TestCasesParametersConfig {

    public final String dirPath;
    public final String execScriptsDirPath;
}
