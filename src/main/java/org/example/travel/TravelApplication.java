package org.example.travel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;

// 直接在@SpringBootApplication中排除WebMvcAutoConfiguration
@SpringBootApplication
@MapperScan("org.example.travel.mapper") // 扫描MyBatis Mapper接口
public class TravelApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelApplication.class, args);
    }
}