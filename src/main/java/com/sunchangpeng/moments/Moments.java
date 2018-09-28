package com.sunchangpeng.moments;

import com.sunchangpeng.timeline.ScanParameter;
import com.sunchangpeng.timeline.ScanParameterBuilder;
import com.sunchangpeng.timeline.Timeline;
import com.sunchangpeng.timeline.TimelineEntry;
import com.sunchangpeng.timeline.message.IMessage;
import com.sunchangpeng.timeline.message.StringMessage;
import com.sunchangpeng.timeline.store.IStore;
import com.sunchangpeng.timeline.store.RedisStore;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

public class Moments {
    private IStore store;
    private IStore sync;

    private List<String> allFriends;

    private final static String endpoint = "";
    private final static String accessKeyID = "";
    private final static String accessKeySecret = "";
    private final static String instanceName = "";
    private final static String storeTableName = "";
    private final static String syncTableName = "";

    public Moments(StringRedisTemplate redisTemplate, List<String> allFriends) {
        this.store = new RedisStore(redisTemplate);
        this.sync = new RedisStore(redisTemplate);
        this.allFriends = allFriends;
    }

    /**
     * 实现一个WeChatMessage，这里为了简单，直接继承StringMessage，且不增加功能，实际中需要考虑处理图片、视频、地理位置、屏蔽等。
     */
    public class WeChatMessage extends StringMessage {
        public WeChatMessage(String content) {
            super(content);
        }
    }

    /**
     * 发布一条状态。
     */
    public void posts(String userId, String content, List<String> allowUsers, List<String> forbidUsers) {
        /**
         * 获取需要发布的用户列表
         */

        List<String> users = getUsersForPost(allowUsers, forbidUsers);

        /**
         * 构造消息对象
         */
        IMessage message = new WeChatMessage("是秋还是冬");

        /**
         * 写入自己的历史状态中
         */
        Timeline timeline = new Timeline(userId, store);
        timeline.store(message);

        /**
         * 发送给朋友圈好友
         */
        for (String user : users) {
            Timeline timeline2 = new Timeline(user, sync);
            timeline2.store(message);
        }
    }

    /**
     * 用户刷新自己的朋友圈。
     */
    public void refresh(String user, long lastSequenceID) {
        Timeline timeline = new Timeline(user, sync);

        /**
         * 构造读取的范围，逆向读取，从最新的数据开始一直读到上次的最新位置，每次返回100个。
         */
        ScanParameter scanParameter = ScanParameterBuilder
                .scanBackward()
                .from(Long.MAX_VALUE)
                .to(lastSequenceID)
                .maxCount(100)
                .build();

        Iterator<TimelineEntry> iterator = timeline.scan(scanParameter);
        while (iterator.hasNext()) {
            TimelineEntry entry = iterator.next();
            String content = new String(entry.getMessage().serialize(), StandardCharsets.UTF_8);
            System.out.println(String.format("sequence_id:%d, message:%s", entry.getSequenceID(), content));
        }
    }

    public void close() {
        store.close();
        sync.close();
    }

    private List<String> getUsersForPost(List<String> allows, List<String> forbids) {
        List<String> allUsers = getAllFriends();
        for (String user : allUsers) {
            if (!forbids.contains(user)) {
                allows.add(user);
            }
        }
        return allows;
    }

    private List<String> getAllFriends() {
        return allFriends;
    }
}
