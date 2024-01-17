package org.example;

import org.example.service.SolutionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MyApplication implements CommandLineRunner {

    private final SolutionService solutionService;

    public MyApplication(SolutionService solutionService) {
        this.solutionService = solutionService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args).close();
    }

    @Override
    public void run(String... args) {
        try {
            solutionService.execute();
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong: " + e);
        }
    }
}
