package com.spendwise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpendWiseApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpendWiseApplication.class, args);
        System.out.println("\n✅ SpendWise is running → http://localhost:8080\n");
    }
}
