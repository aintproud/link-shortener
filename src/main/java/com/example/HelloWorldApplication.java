package com.example;

import org.mindrot.jbcrypt.BCrypt;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@SpringBootApplication
public class HelloWorldApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloWorldApplication.class, args);
        RedisService.declareClient();
    }
}

@RestController
class HelloWorldController {
    @GetMapping("/{id}")
    public ResponseEntity handleR(@PathVariable String id) {
        final Jedis redis = RedisService.getClient();
        final String url = redis.get(id);
        if (url == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT).header("Location", url).body(null);
    }
    @PostMapping("/getId")
    public String servise(@RequestBody final GetIdJson request) {
        final String url = request.getUrl();
        final String id = IdService.generate(url);
        final Jedis redis = RedisService.getClient();
        redis.set(id, url);
        final String bar = redis.get(id);
        System.out.println(bar);
        return id;
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

class IdService {
    public static String generate(String link){
        String salt = "$2a$10$Tdc1oXEY8UPd87VC4kd8GO";
        String hash = BCrypt.hashpw(link, salt);
        String id = hash.substring(29, Math.min(hash.length(), 41));
        return id;
    }
}

class GetIdJson {
    String url;
    public String getUrl() {
        return url;
    }
}