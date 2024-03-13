package rs.ac.kg.fin.albus.hagrid.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Data
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "init-files-parameters")
public class InitParametersConfig {

    public final String dirPath;
}
