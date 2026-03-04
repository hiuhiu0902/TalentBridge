package com.demo.talentbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.demo.talentbridge", "controller", "service", "repository", "config"})
@EntityScan(basePackages = "entity")
@EnableJpaRepositories(basePackages = "repository")
public class TalentBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalentBridgeApplication.class, args);
    }

}
