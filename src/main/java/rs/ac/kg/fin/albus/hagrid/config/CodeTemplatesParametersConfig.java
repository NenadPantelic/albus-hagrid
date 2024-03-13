package rs.ac.kg.fin.albus.hagrid.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Data
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "code-templates-params")
public class CodeTemplatesParametersConfig {

    public final String dirPath;
}
