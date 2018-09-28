package com.sunchangpeng.im;

import com.sunchangpeng.timeline.ScanParameter;
import com.sunchangpeng.timeline.ScanParameterBuilder;
import com.sunchangpeng.timeline.Timeline;
import com.sunchangpeng.timeline.TimelineEntry;
import com.sunchangpeng.timeline.message.IMessage;
import com.sunchangpeng.timeline.message.StringMessage;
import com.sunchangpeng.timeline.store.RedisStore;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SimpleIM {
    private RedisStore store = null;
    private RedisStore sync = null;

    public SimpleIM(StringRedisTemplate redisTemplate) {
        System.out.println("Start create IM");

        store = new RedisStore(redisTemplate);
        sync = new RedisStore(redisTemplate);

        store.drop();
        sync.drop();

        if (!store.exist()) {
            store.create();
        }

        if (!sync.exist()) {
            sync.create();
        }

        System.out.println("End create IM");
    }

    public void push() {
        System.out.println("Start Push message to IM");

        List<String> groupMembers = Arrays.asList("user_A", "user_B", "user_C");

        sendGroupMessage("group_1", new StringMessage("user_B:阿里云的NoSQL数据库是哪个?"), groupMembers);
        sendGroupMessage("group_1", new StringMessage("user_C:是表格存储"), groupMembers);
        sendGroupMessage("group_1", new StringMessage("user_B:好，谢谢"), groupMembers);

        System.out.println("End Push message to IM");
    }

    /**
     * 读取群历史消息内容
     */
    public void getHistoryMessage() {
        System.out.println("Begin Get history message from IM");

        Timeline group = new Timeline("group_1", store);
        ScanParameter scanParameter = ScanParameterBuilder
                .scanBackward()
                .from(Long.MAX_VALUE)
                .to(0)
                .maxCount(100)
                .build();
        Iterator<TimelineEntry> entries = group.scan(scanParameter);
        while (entries.hasNext()) {
            System.out.println(new String(entries.next().getMessage().serialize()));
        }
        System.out.println("End Get history message from IM");
    }

    /**
     * 读取最新的同步消息
     */
    public void getSyncMessage() {
        System.out.println("Begin Get sync message from IM");

        Timeline userSync = new Timeline("user_A", sync);
        ScanParameter scanParameter = ScanParameterBuilder
                .scanForward()
                .from(0)
                .to(Long.MAX_VALUE)
                .maxCount(100)
                .build();
        Iterator<TimelineEntry> iterator = userSync.scan(scanParameter);
        while (iterator.hasNext()) {
            TimelineEntry entry = iterator.next();
            try {
                String content = new String(entry.getMessage().serialize(), "UTF-8");
                System.out.println(String.format("sequence_id:%d, message:%s", entry.getSequenceID(), content));
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
        }
        System.out.println("Begin End sync message from IM");
    }

    private void sendGroupMessage(String groupName, IMessage message, List<String> groupMembers) {
        Timeline sender = new Timeline(groupName, store);
        sender.store(message);

        for (String user : groupMembers) {
            Timeline receiver = new Timeline(user, sync);
            receiver.store(message);
        }
    }

    public void close() {
        store.close();
        sync.close();
    }
}
