package rs.ac.kg.fin.albus.hagrid.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Data
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "environment-parameters")
public class EnvironmentParametersConfig {

    public final String dirPath;
}
