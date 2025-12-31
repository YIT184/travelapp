package org.example.travel.util;

import java.util.UUID;

public class UUIDUtil {
    public static String getUUID() {
        // 去掉UUID中的横线，返回32位字符串
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
}