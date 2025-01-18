package com.kai.util;

import com.kai.exception.ServiceException;

import java.util.Collection;
import java.util.Map;

public class AssertUtils {

    // 断言对象非null
    public static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new ServiceException(message == null ? "Object cannot be null" : message);
        }
    }

//    // 断言对象为null
//    public static void assertNull(Object obj, String message) {
//        if (obj != null) {
//            throw new ServiceException(message == null ? "Object must be null" : message);
//        }
//    }

    // 断言字符串非空
    public static void assertNotEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new ServiceException(message == null ? "String cannot be empty" : message);
        }
    }

//    // 断言字符串为空
//    public static void assertEmpty(String str, String message) {
//        if (str != null && !str.trim().isEmpty()) {
//            throw new ServiceException(message == null ? "String must be empty" : message);
//        }
//    }

    // 断言集合非空
    public static void assertNotEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new ServiceException(message == null ? "Collection cannot be empty" : message);
        }
    }

    // 断言集合为空
    public static void assertEmpty(Collection<?> collection, String message) {
        if (collection != null && !collection.isEmpty()) {
            throw new ServiceException(message == null ? "Collection must be empty" : message);
        }
    }

    // 断言Map非空
    public static void assertNotEmpty(Map<?, ?> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new ServiceException(message == null ? "Map cannot be empty" : message);
        }
    }

    // 断言Map为空
    public static void assertEmpty(Map<?, ?> map, String message) {
        if (map != null && !map.isEmpty()) {
            throw new ServiceException(message == null ? "Map must be empty" : message);
        }
    }
}

