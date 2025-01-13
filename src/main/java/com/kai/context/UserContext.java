package com.kai.context;

import lombok.Getter;

public class UserContext {
    private static final ThreadLocal<UserInfo> USER_INFO_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUserInfo(Long userId, String username) {
        USER_INFO_THREAD_LOCAL.set(new UserInfo(userId, username));
    }

    public static UserInfo getUserInfo() {
        return USER_INFO_THREAD_LOCAL.get();
    }

    public static Long getUserId() {
        UserInfo userInfo = USER_INFO_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    public static String getUsername() {
        UserInfo userInfo = USER_INFO_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getUsername() : null;
    }

    public static void clear() {
        USER_INFO_THREAD_LOCAL.remove();
    }

    @Getter
    public static class UserInfo {
        private final Long userId;
        private final String username;

        public UserInfo(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

    }
}
