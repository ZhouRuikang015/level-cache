package cn.zhouruikang.message;

public enum MessageType {

    EVICT( "删除该缓存名下单key" ),

    CLEAR( "清空该缓存名下全部key");

    MessageType(String description) {
        this.description = description;
    }

    private final String description;

    public String getDescription() {
        return description;
    }

}
