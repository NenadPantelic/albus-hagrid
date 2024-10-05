package rs.ac.kg.fin.albus.hagrid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import rs.ac.kg.fin.albus.hagrid.config.CodeTemplatesParametersConfig;
import rs.ac.kg.fin.albus.hagrid.config.EnvironmentParametersConfig;
import rs.ac.kg.fin.albus.hagrid.scoring.GradingHandler;

@EnableConfigurationProperties(
        {EnvironmentParametersConfig.class, CodeTemplatesParametersConfig.class}
)
@SpringBootApplication
public class HagridApplication {


    public static void main(String[] args) {
        SpringApplication.run(HagridApplication.class, args);
    }

//    @Autowired
//    private GradingHandler gradingHandler;
//
//    @Bean
//    CommandLineRunner runner() {
//        return args -> {
//            //            Map<String, String> map = Map.of(
//            //                    "MYSQL_USER", "albus",
//            //                    "MYSQL_PASSWORD", "pass123",
//            //                    "MYSQL_DATABASE", "test123"
//            //            );
//            // mysql -h 0.0.0.0 -u albus -p pass123 -P 3306 -D test123 < source /home/subm-1.sql
//            gradingHandler.executeAndGrade();
//        };
//
//    }
}