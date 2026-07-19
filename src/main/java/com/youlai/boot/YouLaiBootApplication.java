package com.youlai.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用启动类
 *
 * @author Ray.Hao
 * @since 3.0.0
 */
@EnableScheduling
@SpringBootApplication
public class YouLaiBootApplication {
/**
 * 应用启动入口
 */

    public static void main(String[] args) {
        SpringApplication.run(YouLaiBootApplication.class, args);
    }

}