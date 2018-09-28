package com.sunchangpeng.timeline.message;

import com.sunchangpeng.timeline.TimelineException;
import com.sunchangpeng.timeline.TimelineExceptionType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 一种简单的String类型的消息
 */
public class StringMessage extends DistinctMessage {
    private Map<String, String> attributes = new HashMap<String, String>();
    private String content = null;

    /**
     * 字符串类型消息的构造函数。
     */
    public StringMessage() {
        content = "";
    }

    /**
     * 字符串类型消息的构造函数。
     *
     * @param content 消息内容。
     */
    public StringMessage(String content) {
        this.content = content;
    }

    /**
     * 返回消息内容
     *
     * @return 消息内容
     */
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public byte[] serialize() {
        return content.getBytes();
    }

    @Override
    public void deserialize(byte[] input) {
        content = new String(input);
    }

    @Override
    public void addAttribute(String name, String value) {
        if (name == null || name.isEmpty()) {
            throw new TimelineException(TimelineExceptionType.INVALID_USE, "Attribute name is null or empty.");
        }
        if (value == null || value.isEmpty()) {
            throw new TimelineException(TimelineExceptionType.INVALID_USE,
                    "Attribute value is null or empty.");
        }

        attributes.put(name, value);
    }

    @Override
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(this.attributes);
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
