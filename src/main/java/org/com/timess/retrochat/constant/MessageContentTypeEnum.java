package org.com.timess.retrochat.constant;

import cn.hutool.core.util.ObjUtil;

import java.util.Objects;

/**
 * @author 33363
 * 消息类型枚举类
 */

public enum MessageContentTypeEnum {

    TEXT("TEXT"),

    EMOJI("EMOJI");
    private final String type;

    MessageContentTypeEnum(String type) {
        this.type = type;
    }

    /**
     * 根据value获取枚举对象
     * @param value
     * @return
     */
    public static MessageContentTypeEnum getEnumByType(String value){
        if(ObjUtil.isEmpty(value)){
            return null;
        }
        for (MessageContentTypeEnum messageContentTypeEnum : MessageContentTypeEnum.values()) {
            if(Objects.equals(messageContentTypeEnum.type, value)){
                return messageContentTypeEnum;
            }
        }
        return null;
    }
}
