package com.minimaya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MiniMayaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiniMayaApplication.class, args);
    }
}
