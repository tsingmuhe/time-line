package com.sunchangpeng;

import com.sunchangpeng.im.SimpleIM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootApplication
public class SpringTimelineApplication implements CommandLineRunner {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SpringTimelineApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        SimpleIM im = new SimpleIM(redisTemplate);
        im.push();
        im.getHistoryMessage();
        im.getSyncMessage();
        im.close();
    }
}
