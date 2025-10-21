package org.com.timess.retrochat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.com.timess.retrochat.mapper")
public class RetrochatApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetrochatApplication.class, args);
    }

}
