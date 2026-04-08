package com.next2me.next2view;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Next2ViewApplication {
    public static void main(String[] args) {
        SpringApplication.run(Next2ViewApplication.class, args);
    }
}
