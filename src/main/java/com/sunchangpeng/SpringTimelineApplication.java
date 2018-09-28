package com.sunchangpeng;

import com.sunchangpeng.im.SimpleIM;
import com.sunchangpeng.moments.Moments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.Arrays;

@SpringBootApplication
public class SpringTimelineApplication implements CommandLineRunner {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SpringTimelineApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        im();
        weChat();
    }

    public void im() {
        SimpleIM im = new SimpleIM(redisTemplate);
        im.push();
        im.getHistoryMessage();
        im.getSyncMessage();
        im.close();
    }

    public void weChat() {
        String lily = "user_lily";
        String lucy = "user_lucy";
        Moments weChat = new Moments(redisTemplate, Arrays.asList(lucy));
        weChat.posts(lily, "是秋还是冬", new ArrayList<>(), new ArrayList<>());
        weChat.refresh(lucy, 0);
        weChat.close();
    }
}
