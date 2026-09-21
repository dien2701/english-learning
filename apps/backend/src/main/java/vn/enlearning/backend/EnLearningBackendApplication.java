package vn.enlearning.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EnLearningBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnLearningBackendApplication.class, args);
	}

}
