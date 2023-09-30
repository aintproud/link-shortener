package com.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class HelloWorldApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloWorldApplication.class, args);
        RedisService.declareClient();
    }
}

@RestController
class HelloWorldController {
    @GetMapping("/")
    public String helloWorld() {
        final Jedis redis = RedisService.getClient();
        redis.set("foo", "bar");
        final String bar = redis.get("foo");
        System.out.println(bar);

        return bar;
    }
}

class RedisService{
    public static Jedis resourse;
    public static Jedis getClient(){
        return resourse;
    }
    public static void declareClient(){
        resourse = new JedisPool("localhost", 1000).getResource();
    }
}