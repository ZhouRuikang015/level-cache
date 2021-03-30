package cn.zhouruikang.message;

import java.io.Serializable;

public class Message implements Serializable {

    private MessageType messageType;

    private String cacheName;

    private Object key;

    public Message() {
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Message setMessageType(MessageType messageType) {
        this.messageType = messageType;
        return this;
    }

    public String getCacheName() {
        return cacheName;
    }

    public Message setCacheName(String cacheName) {
        this.cacheName = cacheName;
        return this;
    }

    public Object getKey() {
        return key;
    }

    public Message setKey(Object key) {
        this.key = key;
        return this;
    }
}
