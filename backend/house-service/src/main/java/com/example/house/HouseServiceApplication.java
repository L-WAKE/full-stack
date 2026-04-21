package com.example.house;
// 入口
import com.example.house.common.config.LocalEnvironmentBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HouseServiceApplication {

    public static void main(String[] args) {
        LocalEnvironmentBootstrap.ensureDependenciesReady();
        SpringApplication.run(HouseServiceApplication.class, args);
    }
}
