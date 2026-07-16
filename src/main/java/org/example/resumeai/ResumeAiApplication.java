package org.example.resumeai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("org.example.resumeai.dao")
@SpringBootApplication
public class ResumeAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResumeAiApplication.class, args);
    }
}
