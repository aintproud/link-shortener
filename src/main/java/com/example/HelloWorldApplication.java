package com.example;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class HelloWorldApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        SpringApplication.run(HelloWorldApplication.class);
        RedisService.declareClient(dotenv.get("REDIS_URL"), Integer.parseInt(dotenv.get("REDIS_PORT")));
    }
}

@RestController
class HelloWorldController {
    private final ResourceLoader resourceLoader;

    public HelloWorldController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/")
    public ResponseEntity<String> hello() {
        try {
            // Load the HTML file from the root of the project
            Resource resource = resourceLoader.getResource("file:src/main/resources/static/index.html");
            byte[] fileBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error loading HTML file");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> handleR(@PathVariable String id) {
        final Jedis redis = RedisService.getClient();
        final String url = redis.get(id);
        if (url == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT).header("Location", url).body(null);
    }
    @PostMapping("/getId")
    public ResponseEntity<String> servise(@RequestBody final GetIdJson request) {
        final String url = request.getUrl();
        try {
            new URL(url);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        Dotenv dotenv = Dotenv.configure().load();
        final String id = IdService.generate(url, dotenv.get("BCRYPT_SALT"));
        final Jedis redis = RedisService.getClient();
        redis.set(id, url);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }
}

class RedisService{
    public static Jedis resourse;
    public static Jedis getClient(){
        return resourse;
    }
    public static void declareClient(String host, int port){
        resourse = new JedisPool(host, port).getResource();
    }
}

class IdService {
    public static String generate(String link, String salt){
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

