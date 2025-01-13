package com.kai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
public class ChatAppServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatAppServerApplication.class, args);
    }

}
