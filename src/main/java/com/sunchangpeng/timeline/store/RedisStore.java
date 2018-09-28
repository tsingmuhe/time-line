package com.sunchangpeng.timeline.store;

import com.alibaba.fastjson.JSON;
import com.sunchangpeng.timeline.ScanParameter;
import com.sunchangpeng.timeline.TimelineCallback;
import com.sunchangpeng.timeline.TimelineEntry;
import com.sunchangpeng.timeline.message.IMessage;
import com.sunchangpeng.timeline.message.StringMessage;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class RedisStore implements IStore {
    private final StringRedisTemplate redisTemplate;

    public RedisStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public TimelineEntry write(String timelineID, IMessage message) {
        long score = System.currentTimeMillis();
        if (redisTemplate.opsForZSet().add(timelineID, JSON.toJSONString(message), score)) {
            return new TimelineEntry(score, message);
        }

        throw new RuntimeException();
    }

    @Override
    public void batch(String timelineID, IMessage message) {
        //todo redis pipeline
    }

    @Override
    public Future<TimelineEntry> writeAsync(String timelineID, IMessage message, TimelineCallback<IMessage> callback) {
        return null;
    }

    @Override
    public TimelineEntry read(String timelineID, Long sequenceID) {
        return null;
    }

    @Override
    public Future<TimelineEntry> readAsync(String timelineID, Long sequenceID, TimelineCallback<Long> callback) {
        return null;
    }

    @Override
    public Iterator<TimelineEntry> scan(String timelineID, ScanParameter parameter) {
        if (parameter.isForward()) {
            Set<ZSetOperations.TypedTuple<String>> values = redisTemplate.opsForZSet().rangeByScoreWithScores(timelineID, parameter.getFrom(), parameter.getTo(), 0, parameter.getMaxCount());
            return values.stream().map(item -> new TimelineEntry(item.getScore().longValue(), JSON.parseObject(item.getValue(), StringMessage.class))).collect(Collectors.toList()).iterator();
        }

        Set<ZSetOperations.TypedTuple<String>> values = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(timelineID, parameter.getTo(), parameter.getFrom(), 0, parameter.getMaxCount());
        return values.stream().map(item -> new TimelineEntry(item.getScore().longValue(), JSON.parseObject(item.getValue(), StringMessage.class))).collect(Collectors.toList()).iterator();
    }

    @Override
    public void create() {

    }

    @Override
    public void drop() {
        redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                connection.flushDb();
                return "ok";
            }
        });
    }

    @Override
    public boolean exist() {
        return true;
    }

    @Override
    public void close() {

    }
}
