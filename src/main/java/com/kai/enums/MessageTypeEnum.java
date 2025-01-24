package com.kai.enums;

import com.kai.exception.ServiceException;
import lombok.Getter;

@Getter
public enum MessageTypeEnum {

    GROUP_MESSAGE("group", "群聊消息", "text"),
    GROUP_FILE_MESSAGE("group_file", "群聊文件消息", "file"),

    SINGLE_MESSAGE("single", "单聊消息", "text"),
    SINGLE_FILE_MESSAGE("single_text", "单聊文件消息", "file"),

    NOTIFY_MESSAGE("notify", "通知消息", "text"),

    LOG_MESSAGE("log", "登录消息", "text"),
    LOGOUT_MESSAGE("logout", "登出消息", "text");

//    REGISTER_MESSAGE("register", "注册消息", "text"),
//    UN_REGISTER_MESSAGE("un_register", "取消注册消息", "file");



    MessageTypeEnum(String type, String desc, String payload) {
        this.type = type;
        this.desc = desc;
        this.payload = payload;
    }


    private final String type;
    private final String desc;
    private final String payload;

    public static MessageTypeEnum getEnumByType(String type) {
        for (MessageTypeEnum value : MessageTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        throw new ServiceException("Invalid MessageTypeEnum type: " + type);
    }
}
