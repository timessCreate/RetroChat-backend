package org.com.timess.retrochat.enums;

/**
 * @author eternal
 */

public enum ChatRoomEnums {
    /**
     * 初始化
     */

    PUBLIC(1, "公开群聊"),
    PRIVATE(2, "私密群聊"),
    ONE_TO_ONE(3, "一对一私聊");


    /**
     * 聊天室类型：1-公开群聊 2-私密群聊 3-一对一私聊
     */
    private Integer type;

    private String desc;
    public Integer getType() {
        return type;
    }
    public String getDesc() {
        return desc;
    }

    public void setType(Integer type) {
        this.type = type;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    private ChatRoomEnums(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
