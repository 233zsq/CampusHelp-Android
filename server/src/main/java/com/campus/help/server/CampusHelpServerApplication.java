package com.campus.help.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.campus.help.server.mapper")
public class CampusHelpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusHelpServerApplication.class, args);
    }
}